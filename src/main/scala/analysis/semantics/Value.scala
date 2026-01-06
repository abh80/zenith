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

  case class Float(value: BigDecimal) extends Value {
    override def isZero: Boolean = value == 0

    override def getType: Type = Type.Float
  }

  case class String(value: java.lang.String) extends Value {
    override def getType: Type = Type.String
  }

  case class IdentifierReference(name: Name.Unqualified, referenceType: Type) extends Value {
    override def getType: Type = Type.IdentifierReference

    override def toString: Name.Unqualified = name

    def underlyingType: Type = referenceType
  }

  case class BinaryOp(left: Value, right: Value, op: ast.BinaryOperator) extends Value {
    override def getType: Type = {
      if (left.getType == Type.Float || right.getType == Type.Float) Type.Float
      else Type.Integer 
    }
    override def toString: java.lang.String = s"BinaryOp($left, $op, $right)"
  }

  case class UnaryOp(operand: Value, op: ast.UnaryOperator) extends Value {
    override def getType: Type = operand.getType
    override def toString: java.lang.String = s"UnaryOp($op, $operand)"
  }

  case class InterpolatedString(segments: List[InterpolatedSegment]) extends Value {
    override def getType: Type = Type.String
    override def toString: java.lang.String = s"InterpolatedString(${segments.mkString(", ")})"
  }

  sealed trait InterpolatedSegment
  case class TextSegment(text: java.lang.String) extends InterpolatedSegment
  case class ExprSegment(value: Value) extends InterpolatedSegment
}