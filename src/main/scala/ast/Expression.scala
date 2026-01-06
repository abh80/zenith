package ast

sealed trait Expression

enum BinaryOperator {
  case Add, Subtract, Multiply, Divide, FloorDivide, Modulo, Power, GreaterThan, LessThan, EqualTo
}

enum UnaryOperator {
  case Negate, SquareRoot
}

final case class BinaryExpression(left: AstNode[Expression], right: AstNode[Expression], op: BinaryOperator) extends Expression
final case class UnaryExpression(operand: AstNode[Expression], op: UnaryOperator) extends Expression

sealed trait AstLiteral extends Expression

final case class StringLiteral(value: String) extends AstLiteral

final case class IntegerLiteral(value: String) extends AstLiteral

final case class FloatLiteral(value: String) extends AstLiteral

final case class IdentifierExpression(value: Ast.Id) extends AstLiteral

// String interpolation support
sealed trait StringSegment
final case class TextSegment(text: String) extends StringSegment
final case class ExpressionSegment(expr: AstNode[Expression]) extends StringSegment

final case class InterpolatedString(segments: List[StringSegment]) extends AstLiteral