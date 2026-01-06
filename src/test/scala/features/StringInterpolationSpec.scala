package features

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import testutil.TestHelper

class StringInterpolationSpec extends AnyFunSuite with Matchers {

  def compile(input: String): util.Result[String] = {
    TestHelper.compileString(input)
  }

  test("Simple variable interpolation with $variable syntax") {
    val result = compile("x = 42\nprint \"value is $x\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`value is ${x}`)")
  }

  test("Expression interpolation with $(expression) syntax") {
    val result = compile("a = 5\nb = 10\nprint \"sum is $(a plus b)\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`sum is ${(a + b)}`)")
  }

  test("Multiple variables in one string") {
    val result = compile("a = 5\nb = 10\nprint \"a=$a, b=$b\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`a=${a}, b=${b}`)")
  }

  test("Mixed text and interpolation") {
    val result = compile("x = 100\nprint \"The value of x is $x meters\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`The value of x is ${x} meters`)")
  }

  test("Interpolation at start of string") {
    val result = compile("x = 42\nprint \"$x is the answer\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`${x} is the answer`)")
  }

  test("Interpolation at end of string") {
    val result = compile("x = 42\nprint \"The answer is $x\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`The answer is ${x}`)")
  }

  test("Complex arithmetic in interpolation") {
    val result = compile("a = 2\nb = 3\nc = 4\nprint \"result: $(a plus b times c)\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`result: ${(a + (b * c))}`)")
  }

  test("Nested parentheses in interpolation") {
    val result = compile("a = 2\nb = 3\nc = 4\nprint \"result: $((a plus b) times c)\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`result: ${((a + b) * c)}`)")
  }

  test("Interpolation with squared operator") {
    val result = compile("x = 5\nprint \"x squared is $(x squared)\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`x squared is ${(x ** 2)}`)")
  }

  test("Interpolation with square root") {
    val result = compile("x = 64\nprint \"square root is $(square root of x)\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`square root is ${Math.sqrt(x)}`)")
  }

  test("Interpolation with floor division") {
    val result = compile("a = 10\nb = 3\nprint \"quotient: $(a floor divided by b)\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`quotient: ${Math.floor(a / b)}`)")
  }

  test("Interpolation with modulo") {
    val result = compile("a = 10\nb = 3\nprint \"remainder: $(a modulo b)\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`remainder: ${(a % b)}`)")
  }

  test("Multiple interpolations with expressions") {
    val result = compile("a = 5\nb = 10\nprint \"sum=$(a plus b), product=$(a times b)\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`sum=${(a + b)}, product=${(a * b)}`)")
  }

  test("Interpolation with constant") {
    val result = compile("pi = 3 but constant\nr = 5\nprint \"circumference: $(2 times pi times r)\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`circumference: ${((2 * pi) * r)}`)")
  }

  test("Empty interpolation segments") {
    val result = compile("x = 42\nprint \"$x\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`${x}`)")
  }

  test("Consecutive interpolations") {
    val result = compile("a = 5\nb = 10\nprint \"$a$b\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`${a}${b}`)")
  }

  test("Interpolation with all arithmetic operators") {
    val code = """
      a = 10
      b = 3
      print "add: $(a plus b)"
      print "sub: $(a minus b)"
      print "mul: $(a times b)"
      print "div: $(a divided by b)"
      print "floor: $(a floor divided by b)"
      print "mod: $(a modulo b)"
      print "pow: $(a raised to b)"
    """
    val result = compile(code)
    result.isRight shouldBe true
    val js = result.toOption.get
    js should include("console.log(`add: ${(a + b)}`)")
    js should include("console.log(`sub: ${(a - b)}`)")
    js should include("console.log(`mul: ${(a * b)}`)")
    js should include("console.log(`div: ${(a / b)}`)")
    js should include("console.log(`floor: ${Math.floor(a / b)}`)")
    js should include("console.log(`mod: ${(a % b)}`)")
    js should include("console.log(`pow: ${(a ** b)}`)")
  }

  test("Regular string without interpolation should not use template literals") {
    val result = compile("print \"no variables\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(\"no variables\")")
    code should not include ("`")
  }

  test("Interpolation with variable reference to another variable") {
    val result = compile("x = 5\ny = x\nprint \"y is $y\"")
    result.isRight shouldBe true
    val code = result.toOption.get
    code should include("console.log(`y is ${y}`)")
  }

  test("Complex physics calculation with interpolation") {
    val code = """
      mass = 50
      velocity = 100
      g = 10 but constant
      kinetic = mass times velocity squared divided by 2
      weight = mass times g
      print "Mass: $mass kg"
      print "Velocity: $velocity m/s"
      print "Kinetic Energy: $kinetic J"
      print "Weight: $weight N"
      print "Momentum: $(mass times velocity) kg⋅m/s"
    """
    val result = compile(code)
    result.isRight shouldBe true
    val js = result.toOption.get
    js should include("console.log(`Mass: ${mass} kg`)")
    js should include("console.log(`Velocity: ${velocity} m/s`)")
    js should include("console.log(`Kinetic Energy: ${kinetic} J`)")
    js should include("console.log(`Weight: ${weight} N`)")
    js should include("console.log(`Momentum: ${(mass * velocity)} kg⋅m/s`)")
  }
}
