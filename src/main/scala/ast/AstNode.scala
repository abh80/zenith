package ast

case class AstNode[+T] private(data: T, id: AstNode.Id)

object AstNode {
  private type Id = Int

  private var id: Id = 0
}