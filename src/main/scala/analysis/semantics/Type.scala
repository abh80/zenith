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
        case Float => true
        case _ => false
      }
  }

  sealed trait Float extends Type {
    override def isFloat: Boolean = true

    override def isCompatibleWith(t: Type): Boolean =
      t match {
        case Float => true
        case _ => false
      }
  }

  sealed trait Primitive extends Type {
    override def isPrimitive: Boolean = true

    def bitWidth: Int
  }
  
  sealed trait String extends Type
  
  sealed trait IdentifierReference extends Type

  case object Integer extends Integer {
    override def getDefaultValue: Option[Value] = Some(Value.Integer(0))

    override def isStreamPrintable: Boolean = true

    override def toString = "Integer"
  }

  case object Float extends Float {
    override def getDefaultValue: Option[Value] = Some(Value.Float(0.0))

    override def isStreamPrintable: Boolean = true

    override def toString = "Float"
  }

  case object String extends String {

    override def getDefaultValue: Option[Value] = Some(Value.String(""))

    override def isCompatibleWith(t: Type): Boolean = t match {
      case String => true
      case _ => false
    }

    override def isStreamPrintable: Boolean = true
  }

  case object IdentifierReference extends IdentifierReference {
    override def getDefaultValue: Option[Value] = None

    override def isCompatibleWith(t: Type): Boolean = true

    def isCompatibleWith(value: Value.IdentifierReference, targetType: Type): Boolean = {
      value.underlyingType.isCompatibleWith(targetType)
    }
    
    override def toString = "Identifier"
  }
  
}