package analysis.semantics

import ast.AstNode
import util.Location
import ast.Locations

sealed trait Symbol {
  final def getLoc: Location = Locations.get(getNodeId)

  def getNodeId: AstNode.Id

  def getUnqualifiedName: Name.Unqualified
}

object Symbol {

}
