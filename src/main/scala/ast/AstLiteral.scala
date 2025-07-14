package ast

sealed trait AstLiteral
final case class StringLiteral(value: String) extends AstLiteral
