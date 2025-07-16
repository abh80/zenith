package ast

object Ast {
  type Id = String

  sealed trait TypeDef
  final case class TypeDefString() extends TypeDef
  final case class TypeDefInteger() extends TypeDef
}
