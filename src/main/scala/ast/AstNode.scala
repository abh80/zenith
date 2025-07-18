package ast

import syntax.CompilationContext

case class AstNode[+T] private(data: T, id: AstNode.Id)

object AstNode {
  type Id = Int

  def createNode[T](data: T)(implicit ctx: CompilationContext): AstNode[T] = AstNode(data, ctx.getNextId)
}
