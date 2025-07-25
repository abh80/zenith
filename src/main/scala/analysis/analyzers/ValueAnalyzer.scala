package analysis.analyzers

import analysis.Analyzer
import analysis.semantics.{Type, Value}
import ast.*
import util.{FatalCompilerError, LocationResolver, Result, TypeMismatch, UndeclaredIdentifierReferenced}
import analysis.semantics.Symbol

class ValueAnalyzer(analyzer: Analyzer, node: AstNode[AstLiteral]) extends LocationResolver(node.id) {
  def evaluateInitializer(value: AstLiteral, expectedType: Type): Result[Value] =
    evaluateExpression(value, expectedType)

  private def evaluateExpression(literal: AstLiteral, expectedType: Type): Result[Value] =
    literal match {
      case StringLiteral(value) if expectedType == Type.String =>
        Right(Value.String(value))
      case IntegerLiteral(value)
        if expectedType == Type.Integer =>
        for {
          convertedValue <- integer(value)
        } yield Value.Integer(convertedValue)
      case IdentifierExpression(name) => evaluateVariableReference(name, expectedType)
      case _ => ???
    }

  private def integer(in: String): Result[BigInt] =
    try
      Right(BigInt(in.trim))
    catch
      case _: NumberFormatException => Left(List(FatalCompilerError()))

  private def evaluateVariableReference(name: Ast.Id, expectedType: Type): Result[Value] =
    analyzer.currentScope.lookup(name) match {
      case Some(sym) if analyzer.typeMap(sym.nodeId) == expectedType => Right(Value.IdentifierReference(sym.name, expectedType))
      case Some(sym) => Left(List(TypeMismatch(LocationResolver.getLoc(node.id), analyzer.typeMap(sym.nodeId), expectedType)))
      case None => Right(Value.String(name))
        //Left(List(UndeclaredIdentifierReferenced(LocationResolver.getLoc(node.id), name)))
    }
}
