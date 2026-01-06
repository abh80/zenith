package syntax

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import java.io.File
import scala.io.Source

class LexerSpec extends AnyFunSuite with Matchers {
  
  given ctx: CompilationContext = CompilationContext()

  def tokenize(input: String): Either[List[util.Problem], List[Token]] = {
    val scanner = new Lexer.Scanner(new File("test.zenith"), input.toCharArray)
    scanner.tokenize()
  }

  test("Lexer should tokenize simple identifiers") {
    val result = tokenize("velocity")
    result.isRight shouldBe true
    result.toOption.get should contain(Token.IDENTIFIER("velocity"))
  }

  test("Lexer should tokenize integer literals") {
    val result = tokenize("42")
    result.isRight shouldBe true
    result.toOption.get should contain(Token.INTEGER_LITERAL("42"))
  }

  test("Lexer should tokenize string literals") {
    val result = tokenize("\"hello world\"")
    result.isRight shouldBe true
    result.toOption.get should contain(Token.STRING_LITERAL("hello world"))
  }

  test("Lexer should tokenize interpolated strings with simple variables") {
    val result = tokenize("\"velocity is $velocity\"")
    result.isRight shouldBe true
    result.toOption.get should contain(Token.INTERPOLATED_STRING_LITERAL("velocity is $velocity"))
  }

  test("Lexer should tokenize interpolated strings with expressions") {
    val result = tokenize("\"result is $(a plus b)\"")
    result.isRight shouldBe true
    result.toOption.get should contain(Token.INTERPOLATED_STRING_LITERAL("result is $(a plus b)"))
  }

  test("Lexer should tokenize keywords") {
    val result = tokenize("but constant mutable print is")
    result.isRight shouldBe true
    val tokens = result.toOption.get
    tokens should contain(Token.BUT())
    tokens should contain(Token.CONSTANT())
    tokens should contain(Token.MUTABLE())
    tokens should contain(Token.PRINT())
    tokens should contain(Token.IS())
  }

  test("Lexer should tokenize assignment operator") {
    val result = tokenize("x = 5")
    result.isRight shouldBe true
    result.toOption.get should contain(Token.ASSIGNMENT())
  }

  test("Lexer should tokenize parentheses") {
    val result = tokenize("(a plus b)")
    result.isRight shouldBe true
    val tokens = result.toOption.get
    tokens should contain(Token.LPAREN())
    tokens should contain(Token.RPAREN())
  }

  test("Lexer should handle escape sequences in strings") {
    val result = tokenize("\"hello\\nworld\"")
    result.isRight shouldBe true
    result.toOption.get should contain(Token.STRING_LITERAL("hello\nworld"))
  }

  test("Lexer should tokenize type definitions") {
    val result = tokenize("string number")
    result.isRight shouldBe true
    val tokens = result.toOption.get
    tokens should contain(Token.TYPE_STRING())
    tokens should contain(Token.TYPE_INTEGER())
  }

  test("Lexer should handle multiple interpolations in one string") {
    val result = tokenize("\"a=$a, b=$b, sum=$(a plus b)\"")
    result.isRight shouldBe true
    result.toOption.get should contain(Token.INTERPOLATED_STRING_LITERAL("a=$a, b=$b, sum=$(a plus b)"))
  }

  test("Lexer should distinguish between regular and interpolated strings") {
    val regular = tokenize("\"no variables here\"")
    val interpolated = tokenize("\"has $variable\"")
    
    regular.isRight shouldBe true
    interpolated.isRight shouldBe true
    
    regular.toOption.get should contain(Token.STRING_LITERAL("no variables here"))
    interpolated.toOption.get should contain(Token.INTERPOLATED_STRING_LITERAL("has $variable"))
  }

  test("Lexer should handle hexadecimal numbers") {
    val result = tokenize("0xFF")
    result.isRight shouldBe true
    result.toOption.get should contain(Token.INTEGER_LITERAL("0xFF"))
  }

  test("Lexer should handle line endings") {
    val result = tokenize("a = 5\nb = 10")
    result.isRight shouldBe true
    result.toOption.get should contain(Token.EOL())
  }
}
