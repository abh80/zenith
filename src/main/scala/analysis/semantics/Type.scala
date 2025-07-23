package analysis.semantics

import ast.AstNode

sealed trait Type {
  def getDefaultValue: Option[Value]

  def getDecNodeId: Option[AstNode.Id] = None

  def getUnderlyingType: Type = this

  def isNumeric: Boolean = isInteger || isFloat

  def isInteger: Boolean = false

  def isFloat: Boolean = false

  def isPrimitive: Boolean = false

  def toString: String

  def isStreamPrintable: Boolean = false

  def isCompatibleWith(t: Type): Boolean
}

object Type {
  sealed trait Integer extends Type {
    override def isInteger: Boolean = true

    override def isCompatibleWith(t: Type): Boolean =
      t match {
        case Integer => true
        case _ => false
      }
  }

  sealed trait Primitive extends Type {
    override def isPrimitive: Boolean = true

    def bitWidth: Int
  }

  case object Integer extends Integer {
    override def getDefaultValue: Option[Value] = Some(Value.Integer(0))

    override def isStreamPrintable: Boolean = true

    override def toString: String = "Integer"
  }
}