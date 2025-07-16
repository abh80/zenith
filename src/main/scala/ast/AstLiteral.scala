package ast

sealed trait AstLiteral

final case class StringLiteral(value: String) extends AstLiteral

final case class IntegerLiteral(value: String) extends AstLiteral
