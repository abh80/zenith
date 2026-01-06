package ast

import ast.Ast.Id

sealed trait Declaration

final case class DecConstant(name: Id, value: AstNode[Expression], typeDef: Option[Ast.TypeDef] = None) extends Declaration

final case class DecMutable(name: Id, value: AstNode[Expression], typeDef: Option[Ast.TypeDef] = None) extends Declaration

final case class PrintStatement(expression: AstNode[Expression]) extends Declaration

final case class IfStatement(condition: AstNode[Expression], thenBlock: List[AstNode[Declaration]], elseBlock: Option[List[AstNode[Declaration]]]) extends Declaration