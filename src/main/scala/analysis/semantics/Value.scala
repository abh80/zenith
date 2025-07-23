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
}