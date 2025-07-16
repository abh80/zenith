package ast

case class AstNode[+T] private(data: T, id: AstNode.Id)

object AstNode {
  type Id = Int
  private var id: Id = 0

  private def getId: Id = {
    val temp = id
    id += 1
    temp
  }

  def createNode[T](data: T): AstNode[T] = AstNode(data, getId)
}