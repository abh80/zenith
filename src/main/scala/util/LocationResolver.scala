package util

import ast.{AstNode, Locations}

trait LocationResolver(nodeId: AstNode.Id) {
  def getLoc(id: AstNode.Id): Location = Locations.getOpt(id).get

  def getLoc: Location = Locations.getOpt(nodeId).get
}

object LocationResolver {
  def getLoc(id: AstNode.Id): Location = Locations.getOpt(id).get
}