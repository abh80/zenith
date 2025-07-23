package ast

trait AstVisitor extends Iterator[AstNode[?]]{
  protected val nodes: List[AstNode[?]]
  protected var currentIndex: Int = 0

  override def hasNext: Boolean = currentIndex < nodes.length

  override def next(): AstNode[?] = {
    if (!hasNext) throw new NoSuchElementException
    val node = nodes(currentIndex)
    currentIndex += 1
    node
  }

  def peek(): Option[AstNode[?]] =
    if (hasNext) Some(nodes(currentIndex)) else None

  def reset(): Unit = currentIndex = 0

  override def foldLeft[A] (initial: A) (f: (A, AstNode[?]) => A): A = {
    nodes.foldLeft(initial)(f)
  }
}
