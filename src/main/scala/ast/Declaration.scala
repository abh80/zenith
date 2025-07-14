package ast

sealed trait Declaration extends AstNode[AstLiteral]
final case class VarDeclaration(name: String, initialVal: Expression, isConstant: Boolean)