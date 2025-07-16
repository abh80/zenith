package ast

import ast.Ast.Id

sealed trait Declaration

final case class DecConstant(name: Id, value: AstNode[AstLiteral]) extends Declaration

final case class DecMutable(name: Id, value: AstNode[AstLiteral]) extends Declaration