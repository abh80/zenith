package analysis.semantics

sealed trait Value {
  def isZero: Boolean = false
  def getType: Type
  def toString: String
}

object Value {
  case class Integer(value: BigInt) extends Value {
    override def isZero: Boolean = value == 0

    override def getType: Type = Type.Integer
  }

  case class String(value: java.lang.String) extends Value {
    override def getType: Type = Type.String
  }

  case class IdentifierReference(name: Name.Unqualified, referenceType: Type) extends Value {
    override def getType: Type = Type.IdentifierReference

    override def toString: Name.Unqualified = name

    def underlyingType: Type = referenceType
  }
}