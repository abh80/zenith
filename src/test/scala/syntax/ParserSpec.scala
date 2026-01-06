package syntax

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import ast._
import java.io.File

class ParserSpec extends AnyFunSuite with Matchers {

  given ctx: CompilationContext = CompilationContext()

  def parse(input: String): util.Result[List[AstNode[_]]] = {
    val file = new File("test.zenith")
    val scanner = new Lexer.Scanner(file, input.toCharArray)
    val tokens = scanner.tokenize() match {
      case Left(errors) => return Left(errors)
      case Right(t) => t
    }
    Parser.parseInputFile(Parser.elementSequence)(file)
  }

  test("Parser should parse simple variable declaration") {
    val result = parse("velocity = 100")
    result.isRight shouldBe true
    val decls = result.toOption.get
    decls should have size 1
  }

  test("Parser should parse constant declaration") {
    val result = parse("g = 10 but constant")
    result.isRight shouldBe true
    val decls = result.toOption.get
    decls should have size 1
    decls.head.data shouldBe a[DecConstant]
  }

  test("Parser should parse mutable declaration") {
    val result = parse("velocity = 100 but mutable")
    result.isRight shouldBe true
    val decls = result.toOption.get
    decls should have size 1
    decls.head.data shouldBe a[DecMutable]
  }

  test("Parser should parse print statement with string literal") {
    val result = parse("print \"hello\"")
    result.isRight shouldBe true
    val decls = result.toOption.get
    decls should have size 1
    decls.head.data shouldBe a[PrintStatement]
  }

  test("Parser should parse print statement with interpolated string") {
    val result = parse("print \"velocity is $velocity\"")
    result.isRight shouldBe true
    val decls = result.toOption.get
    decls should have size 1
    val printStmt = decls.head.data.asInstanceOf[PrintStatement]
    printStmt.expression.data shouldBe a[InterpolatedString]
  }

  test("Parser should parse interpolated string with simple variable") {
    val result = parse("print \"value: $x\"")
    result.isRight shouldBe true
    val decls = result.toOption.get
    val printStmt = decls.head.data.asInstanceOf[PrintStatement]
    val interpolated = printStmt.expression.data.asInstanceOf[InterpolatedString]
    interpolated.segments should have size 2
    interpolated.segments.head shouldBe a[TextSegment]
    interpolated.segments(1) shouldBe a[ExpressionSegment]
  }

  test("Parser should parse interpolated string with expression") {
    val result = parse("print \"result: $(a plus b)\"")
    result.isRight shouldBe true
    val decls = result.toOption.get
    val printStmt = decls.head.data.asInstanceOf[PrintStatement]
    val interpolated = printStmt.expression.data.asInstanceOf[InterpolatedString]
    interpolated.segments should have size 2
  }

  test("Parser should parse arithmetic expressions with plus") {
    val result = parse("x = a plus b")
    result.isRight shouldBe true
    val decls = result.toOption.get
    val decl = decls.head.data.asInstanceOf[DecMutable]
    decl.value.data shouldBe a[BinaryExpression]
  }

  test("Parser should parse arithmetic expressions with times") {
    val result = parse("x = a times b")
    result.isRight shouldBe true
    val decls = result.toOption.get
    val decl = decls.head.data.asInstanceOf[DecMutable]
    val expr = decl.value.data.asInstanceOf[BinaryExpression]
    expr.op shouldBe BinaryOperator.Multiply
  }

  test("Parser should parse squared operator") {
    val result = parse("x = velocity squared")
    result.isRight shouldBe true
    val decls = result.toOption.get
    val decl = decls.head.data.asInstanceOf[DecMutable]
    decl.value.data shouldBe a[BinaryExpression]
  }

  test("Parser should parse square root operator") {
    val result = parse("x = square root of 64")
    result.isRight shouldBe true
    val decls = result.toOption.get
    val decl = decls.head.data.asInstanceOf[DecMutable]
    decl.value.data shouldBe a[UnaryExpression]
  }

  test("Parser should parse floor division") {
    val result = parse("x = a floor divided by b")
    result.isRight shouldBe true
    val decls = result.toOption.get
    val decl = decls.head.data.asInstanceOf[DecMutable]
    val expr = decl.value.data.asInstanceOf[BinaryExpression]
    expr.op shouldBe BinaryOperator.FloorDivide
  }

  test("Parser should parse modulo operator") {
    val result = parse("x = a modulo b")
    result.isRight shouldBe true
    val decls = result.toOption.get
    val decl = decls.head.data.asInstanceOf[DecMutable]
    val expr = decl.value.data.asInstanceOf[BinaryExpression]
    expr.op shouldBe BinaryOperator.Modulo
  }

  test("Parser should parse parenthesized expressions") {
    val result = parse("x = (a plus b) times c")
    result.isRight shouldBe true
    val decls = result.toOption.get
    decls should have size 1
  }

  test("Parser should parse type annotations") {
    val result = parse("x is number = 42")
    result.isRight shouldBe true
    val decls = result.toOption.get
    val decl = decls.head.data.asInstanceOf[DecMutable]
    decl.typeDef shouldBe defined
  }

  test("Parser should parse multiple declarations") {
    val result = parse("a = 5\nb = 10\nc = 15")
    result.isRight shouldBe true
    val decls = result.toOption.get
    decls should have size 3
  }

  test("Parser should parse complex interpolated string") {
    val result = parse("print \"a=$a, b=$b, sum=$(a plus b)\"")
    result.isRight shouldBe true
    val decls = result.toOption.get
    val printStmt = decls.head.data.asInstanceOf[PrintStatement]
    val interpolated = printStmt.expression.data.asInstanceOf[InterpolatedString]
    interpolated.segments.size should be > 3
  }

  test("Parser should parse nested expressions in interpolation") {
    val result = parse("print \"result: $(a times b plus c)\"")
    result.isRight shouldBe true
    val decls = result.toOption.get
    val printStmt = decls.head.data.asInstanceOf[PrintStatement]
    printStmt.expression.data shouldBe a[InterpolatedString]
  }

  test("Parser should handle unquoted identifier sequences as strings") {
    val result = parse("msg = Hello World")
    result.isRight shouldBe true
    val decls = result.toOption.get
    val decl = decls.head.data.asInstanceOf[DecMutable]
    decl.value.data shouldBe a[StringLiteral]
  }
}
