package analysis.analyzers

import analysis.Analyzer
import analysis.semantics.{Type, Value}
import ast.*
import util.{FatalCompilerError, LocationResolver, Result}

class ValueAnalyzer(analyzer: Analyzer, node: AstNode[AstLiteral]) extends LocationResolver(node.id) {
  def evaluateInitializer(value: AstLiteral, expectedType: Type): Result[Value] =
    evaluateExpression(value, expectedType)

  def evaluateExpression(literal: AstLiteral, expectedType: Type): Result[Value] =
    literal match {
      case StringLiteral(value) => ???
      case IntegerLiteral(value)
        if expectedType == Type.Integer =>
        for {
          convertedValue <- integer(value)
        } yield Value.Integer(convertedValue)
      case IdentifierExpression(value) => ???
    }

  private def integer(in: String): Result[BigInt] =
    try
      Right(BigInt(in.trim))
    catch
      case _: NumberFormatException => Left(List(FatalCompilerError()))
}
