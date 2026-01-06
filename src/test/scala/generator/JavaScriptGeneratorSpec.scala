package generator

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import testutil.TestHelper

class JavaScriptGeneratorSpec extends AnyFunSuite with Matchers {

  def compile(input: String): util.Result[String] = {
    TestHelper.compileString(input)
  }

  test("Generator should generate simple variable declaration") {
    val result = compile("velocity = 100")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("let velocity = 100")
  }

  test("Generator should generate constant declaration") {
    val result = compile("g = 10 but constant")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("const g = 10")
  }

  test("Generator should generate print statement") {
    val result = compile("print \"hello\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(\"hello\")")
  }

  test("Generator should generate interpolated string with variable") {
    val result = compile("velocity = 100\nprint \"velocity is $velocity\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`velocity is ${velocity}`)")
  }

  test("Generator should generate interpolated string with expression") {
    val result = compile("a = 5\nb = 10\nprint \"sum is $(a plus b)\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`sum is ${(a + b)}`)")
  }

  test("Generator should generate addition expression") {
    val result = compile("a = 1\nb = 2\nx = a plus b")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("(a + b)")
  }

  test("Generator should generate multiplication expression") {
    val result = compile("a = 1\nb = 2\nx = a times b")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("(a * b)")
  }

  test("Generator should generate squared expression") {
    val result = compile("velocity = 10\nx = velocity squared")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("** 2")
  }

  test("Generator should generate square root expression") {
    val result = compile("x = square root of 64")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("Math.sqrt")
  }

  test("Generator should generate floor division") {
    val result = compile("a = 10\nb = 3\nx = a floor divided by b")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("Math.floor")
  }

  test("Generator should generate modulo expression") {
    val result = compile("a = 10\nb = 3\nx = a modulo b")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("%")
  }

  test("Generator should generate power expression") {
    val result = compile("a = 2\nb = 3\nx = a raised to b")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("**")
  }

  test("Generator should generate negation expression") {
    val result = compile("a = 1\nx = negative a")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("(-a)")
  }

  test("Generator should generate type comments") {
    val result = compile("x is number = 42")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("@type {number}")
  }

  test("Generator should generate complex interpolated string") {
    val result = compile("a = 5\nb = 10\nprint \"a=$a, b=$b, sum=$(a plus b)\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`a=${a}, b=${b}, sum=${(a + b)}`)")
  }

  test("Generator should generate nested arithmetic in interpolation") {
    val result = compile("a = 2\nb = 3\nc = 4\nprint \"result: $(a plus b times c)\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`result: ${(a + (b * c))}`)")
  }

  test("Generator should handle multiple print statements") {
    val result = compile("print \"first\"\nprint \"second\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(\"first\")")
    code should include("console.log(\"second\")")
  }

  test("Generator should generate string literals correctly") {
    val result = compile("msg = Hello World\nprint msg")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("\"Hello World\"")
  }

  test("Generator should handle complex physics calculation") {
    val input = """
      mass = 50
      velocity = 100
      kinetic = mass times velocity squared divided by 2
      print "Kinetic energy: $kinetic J"
    """
    val result = compile(input)
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("let mass = 50")
    code should include("let velocity = 100")
    code should include("let kinetic")
    code should include("console.log(`Kinetic energy: ${kinetic} J`)")
  }
}
