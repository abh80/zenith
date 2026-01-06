package analysis.analyzers

import analysis.Analyzer
import analysis.semantics.{Type, Value}
import ast.*
import util.{FatalCompilerError, LocationResolver, Result, TypeMismatch, UndeclaredIdentifierReferenced}
import analysis.semantics.Symbol

class ValueAnalyzer(analyzer: Analyzer, node: AstNode[Expression]) extends LocationResolver(node.id) {
  def evaluateInitializer(value: Expression, expectedType: Type): Result[Value] =
    evaluateExpression(value, expectedType)

  private def evaluateExpression(literal: Expression, expectedType: Type): Result[Value] =
    literal match {
      case StringLiteral(value) if expectedType == Type.String =>
        Right(Value.String(value))
      case IntegerLiteral(value)
        if expectedType == Type.Integer =>
        for {
          convertedValue <- integer(value)
        } yield Value.Integer(convertedValue)
      case IntegerLiteral(value)
        if expectedType == Type.Float =>
        for {
          convertedValue <- integer(value)
        } yield Value.Float(BigDecimal(convertedValue))
      case FloatLiteral(value)
        if expectedType == Type.Float =>
        for {
          convertedValue <- float(value)
        } yield Value.Float(convertedValue)
      case IdentifierExpression(name) => evaluateVariableReference(name, expectedType)
      case BinaryExpression(left, right, op) =>
        val leftType = inferExpressionType(left.data)
        val rightType = inferExpressionType(right.data)
        for {
          leftVal <- evaluateExpression(left.data, leftType)
          rightVal <- evaluateExpression(right.data, rightType)
        } yield Value.BinaryOp(leftVal, rightVal, op)
      case UnaryExpression(operand, op) =>
        for {
          valVal <- evaluateExpression(operand.data, expectedType)
        } yield Value.UnaryOp(valVal, op)
      case InterpolatedString(segments) if expectedType == Type.String =>
        evaluateInterpolatedString(segments)
      case _ => ???
    }

  private def evaluateInterpolatedString(segments: List[StringSegment]): Result[Value] = {
    val evaluatedSegments = segments.map {
      case TextSegment(text) => Right(Value.TextSegment(text))
      case ExpressionSegment(exprNode) =>
        // Try to evaluate as any type, we'll convert to string during code generation
        val exprType = inferExpressionType(exprNode.data)
        evaluateExpression(exprNode.data, exprType).map(Value.ExprSegment(_))
    }
    
    val (errors, values) = evaluatedSegments.partitionMap(identity)
    if (errors.nonEmpty) Left(errors.flatten)
    else Right(Value.InterpolatedString(values))
  }

  private def inferExpressionType(expr: Expression): Type = expr match {
    case _: StringLiteral | _: InterpolatedString => Type.String
    case _: IntegerLiteral => Type.Integer
    case _: FloatLiteral => Type.Float
    case IdentifierExpression(name) =>
      analyzer.currentScope.lookup(name).map(sym => analyzer.typeMap(sym.nodeId)).getOrElse(Type.String)
    case BinaryExpression(left, right, op) =>
       // Simplified inference for now
       val leftType = inferExpressionType(left.data)
       val rightType = inferExpressionType(right.data)
       import ast.BinaryOperator.*
       if (op == Add && (leftType == Type.String || rightType == Type.String)) Type.String
       else if (op == GreaterThan || op == LessThan || op == EqualTo) Type.Integer
       else if (leftType == Type.Float || rightType == Type.Float) Type.Float
       else Type.Integer 
  }

  private def float(in: String): Result[BigDecimal] =
    try
      Right(BigDecimal(in.trim))
    catch
      case _: NumberFormatException => Left(List(FatalCompilerError()))

  private def integer(in: String): Result[BigInt] =
    try
      Right(BigInt(in.trim))
    catch
      case _: NumberFormatException => Left(List(FatalCompilerError()))

  private def evaluateVariableReference(name: Ast.Id, expectedType: Type): Result[Value] =
    analyzer.currentScope.lookup(name) match {
      case Some(sym) if analyzer.typeMap(sym.nodeId).isCompatibleWith(expectedType) =>
         // Need to handle promotion if types differ but are compatible
         val actualType = analyzer.typeMap(sym.nodeId)
         if (actualType == Type.Integer && expectedType == Type.Float) {
            // Implicit promotion from referencing an integer variable where float is expected
            // We return a wrapped reference that implies promotion? 
            // Value.IdentifierReference stores referenceType.
            // If we return Reference with Integer type, it might later clash if we expect Float value.
            // But Value.IdentifierReference is just looking up.
            // When generating code, if it's an IdentifierReference, we emit symbol name.
            // JS handles number promotion.
            Right(Value.IdentifierReference(sym.name, expectedType))
         } else {
            Right(Value.IdentifierReference(sym.name, expectedType))
         }
      case Some(sym) => Left(List(TypeMismatch(LocationResolver.getLoc(node.id), analyzer.typeMap(sym.nodeId), expectedType)))
      case None => Right(Value.String(name))
        //Left(List(UndeclaredIdentifierReferenced(LocationResolver.getLoc(node.id), name)))
    }
}
