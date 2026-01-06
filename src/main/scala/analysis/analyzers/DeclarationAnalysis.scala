package analysis.analyzers

import analysis.Analyzer
import analysis.semantics.Symbol.ConstantSymbol
import analysis.semantics.{Scope, Symbol, Type, Value}
import ast.*
import util.*

class DeclarationAnalysis(analyzer: Analyzer, node: AstNode[Declaration]) extends LocationResolver(node.id) {
  def analyzeConstantDeclaration(decl: DecConstant, nodeId: AstNode.Id): Result[Analyzer] =
    for {
      declaredType <- inferOrValidateType(decl.typeDef, decl.value)
      symbol = ConstantSymbol(node.asInstanceOf[AstNode[DecConstant]])
      updatedScope <- AnalysisUtil.declareSymbol(analyzer.currentScope, symbol)
      initialValue <- new ValueAnalyzer(analyzer, decl.value).evaluateInitializer(decl.value.data, declaredType)
    } yield updateAnalyzerWithDeclaration(analyzer, symbol, updatedScope, declaredType, initialValue)

  def analyzeMutableDeclaration(decl: DecMutable, nodeId: AstNode.Id): Result[Analyzer] =
    for {
      declaredType <- inferOrValidateType(decl.typeDef, decl.value)
      symbol = Symbol.MutableSymbol(node.asInstanceOf[AstNode[DecMutable]])
      updatedScope <- AnalysisUtil.declareSymbol(analyzer.currentScope, symbol)
      initialValue <- new ValueAnalyzer(analyzer, decl.value).evaluateInitializer(decl.value.data, declaredType)
    } yield updateAnalyzerWithDeclaration(analyzer, symbol, updatedScope, declaredType, initialValue)

  def analyzePrintStatement(stmt: PrintStatement, nodeId: AstNode.Id): Result[Analyzer] =
    for {
      exprType <- inferExpressionType(stmt.expression)
      exprValue <- new ValueAnalyzer(analyzer, stmt.expression).evaluateInitializer(stmt.expression.data, exprType)
    } yield analyzer.copy(
      valueMap = analyzer.valueMap + (node.id -> exprValue),
      typeMap = analyzer.typeMap + (node.id -> exprType),
      printStatements = analyzer.printStatements :+ node.asInstanceOf[AstNode[PrintStatement]]
    )

  private def inferOrValidateType(typeDefOpt: Option[Ast.TypeDef], value: AstNode[Expression]): Result[Type] =
    typeDefOpt match {
      case Some(typeDef) =>
        for {
          annotatedType <- resolveTypeAnnotation(typeDef)
          // I am using expression since later we might have to add operations
          expressionType <- inferExpressionType(value)
          _ <- validateTypeCompatibility(annotatedType, expressionType)
        } yield annotatedType

      case None =>
        inferExpressionType(value)
    }

  private def resolveTypeAnnotation(typeDef: Ast.TypeDef): Result[Type] =
    typeDef match {
      case Ast.TypeDefString() => Right(Type.String)
      case Ast.TypeDefInteger() => Right(Type.Integer)
      case _ => Left(List(UnknownTypeDefinition(getLoc, "Unknown type definition")))
    }

  private def inferExpressionType(exprNode: AstNode[Expression]): Result[Type] =
    exprNode.data match {
      case StringLiteral(value) => Right(Type.String)
      case IntegerLiteral(value) => Right(Type.Integer)
      case InterpolatedString(_) => Right(Type.String)
      case IdentifierExpression(value) =>
        analyzer.currentScope.lookup(value) match {
          case Some(sym) => Right(analyzer.typeMap(sym.nodeId))
          case None => Right(Type.String)
            // Left(List(UndeclaredIdentifierReferenced(getLoc(exprNode.id), value)))
        }
      case BinaryExpression(left, right, op) =>
        for {
          leftType <- inferExpressionType(left)
          rightType <- inferExpressionType(right)
          // TODO: Check for type compatibility (e.g. arithmetic on integers)
        } yield Type.Integer
      case UnaryExpression(operand, op) =>
         inferExpressionType(operand).map(_ => Type.Integer)
      case _ => Left(List(UnknownTypeDefinition(Locations.getOpt(exprNode.id).get, "Unknown type definition")))
    }

  private def validateTypeCompatibility(expected: Type, actual: Type): Result[Unit] = {
    if (!expected.isCompatibleWith(actual)) then
      Left(List(TypeMismatch(getLoc, expected, actual)))
    else
      Right(())
  }

  private def updateAnalyzerWithDeclaration(analyzer: Analyzer, symbol: Symbol, scope: Scope, declaredType: Type, value: Value): Analyzer =
    analyzer.copy(currentScope = scope, symbolScopeMap = analyzer.symbolScopeMap + (symbol -> analyzer.currentScope),
      typeMap = analyzer.typeMap + (node.id -> declaredType),
      valueMap = analyzer.valueMap + (node.id -> value),
      usedSymbolSet = analyzer.usedSymbolSet + symbol
    )
}
