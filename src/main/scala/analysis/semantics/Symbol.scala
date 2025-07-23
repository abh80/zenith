package analysis.semantics

import analysis.semantics.Name.Unqualified
import ast.AstNode.Id
import ast.{AstNode, DecConstant, DecMutable, Locations}
import util.Location

/** Represents a symbol in the program with its location and name information */
sealed trait Symbol {
  /** Gets the location of the symbol, if available */
  def getLocation: Option[Location] = Locations.getOpt(nodeId)

  /** Gets the unique identifier of the AST node */
  def nodeId: Id

  /** Gets the unqualified name of the symbol */
  def name: Name.Unqualified
}

object Symbol {
  private type DeclarationNode[T] = AstNode[T]
  
  protected abstract class AbstractSymbol[T](protected val node: DeclarationNode[T]) extends Symbol {
    override def nodeId: Id = node.id
  }

  /** Represents a constant declaration in the program */
  final case class ConstantSymbol(override val node: DeclarationNode[DecConstant])
    extends AbstractSymbol[DecConstant](node) {
    override def name: Unqualified = node.data.name
  }

  /** Represents a mutable variable declaration in the program */
  final case class MutableSymbol(override val node: DeclarationNode[DecMutable])
    extends AbstractSymbol[DecMutable](node) {
    override def name: Unqualified = node.data.name
  }
}