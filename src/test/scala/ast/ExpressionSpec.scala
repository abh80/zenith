package ast

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ExpressionSpec extends AnyFunSuite with Matchers {

  test("StringLiteral should store value") {
    val literal = StringLiteral("hello")
    literal.value shouldBe "hello"
  }

  test("IntegerLiteral should store value") {
    val literal = IntegerLiteral("42")
    literal.value shouldBe "42"
  }

  test("IdentifierExpression should store identifier") {
    val expr = IdentifierExpression("velocity")
    expr.value shouldBe "velocity"
  }

  test("TextSegment should store text") {
    val segment = TextSegment("Hello World")
    segment.text shouldBe "Hello World"
  }

  test("BinaryOperator should have all arithmetic operators") {
    BinaryOperator.Add should not be null
    BinaryOperator.Subtract should not be null
    BinaryOperator.Multiply should not be null
    BinaryOperator.Divide should not be null
    BinaryOperator.FloorDivide should not be null
    BinaryOperator.Modulo should not be null
    BinaryOperator.Power should not be null
  }

  test("UnaryOperator should have negate and square root") {
    UnaryOperator.Negate should not be null
    UnaryOperator.SquareRoot should not be null
  }

  test("InterpolatedString with only text segments") {
    val segments = List(TextSegment("Just plain text"))
    val interpolated = InterpolatedString(segments)
    interpolated.segments should have size 1
    interpolated.segments.head shouldBe a[TextSegment]
  }
}
