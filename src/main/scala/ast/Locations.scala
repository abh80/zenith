package ast

import util.Location

import scala.collection.mutable

object Locations {

  private val store = mutable.HashMap[AstNode.Id, Location]()

  def put(id: AstNode.Id, loc: Location): Unit = store.put(id, loc)

  def getOpt(id: AstNode.Id): Option[Location] = store.get(id)
  
  def getMapImmutable: Map[AstNode.Id, Location] = store.clone().toMap
}
