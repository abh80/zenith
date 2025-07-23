package analysis.semantics

import scala.collection.mutable

/**
 * Represents a single lexical scope.
 *
 * @param parent The parent scope, if one exists. This allows for chained lookups.
 * @param level The nesting depth of this scope.
 */
class Scope(val parent: Option[Scope], val level: Int) {

  private val symbols = mutable.Map.empty[Name.Unqualified, Symbol]

  /**
   * Defines a symbol within this specific scope.
   * Fails if the symbol name already exists.
   *
   * @return true if successful, false on redeclaration.
   */
  def define(symbol: Symbol): Boolean = {
    val name = symbol.name
    if (symbols.contains(name)) {
      false // Redeclaration in the same scope
    } else {
      symbols.put(name, symbol)
      true
    }
  }

  /**
   * Looks up a symbol by name, starting in this scope and then searching
   * in parent scopes if not found.
   */
  def lookup(name: Name.Unqualified): Option[Symbol] = {
    symbols.get(name) match {
      case Some(symbol) => Some(symbol) // Found in this scope
      case None => parent.flatMap(_.lookup(name)) // Search in parent
    }
  }
}

object Scope {
  def createGlobal() : Scope = new Scope(parent = None, 0)
}