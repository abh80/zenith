package generator

import analysis.Analyzer
import analysis.semantics.{Scope, Type, Value}
import ast.{Ast, AstNode}
import util.{FatalCompilerError, Result}

case class GeneratorContext(analyzer: Analyzer, currentScope: Scope, indentLevel: Int = 0, parentNode: Option[AstNode[?]] = None) {
  def withIndent(level: Int): GeneratorContext =
    copy(indentLevel = level)

  def incrementIndent(): GeneratorContext =
    copy(indentLevel = indentLevel + 1)

  def getSymbolType(nodeId: AstNode.Id): Option[Type] =
    analyzer.typeMap.get(nodeId)

  def getSymbolValue(nodeId: AstNode.Id): Option[Value] =
    analyzer.valueMap.get(nodeId)

  def indent: String = "  " * indentLevel

  def getTypeDefFromSymbol(nodeId: AstNode.Id) : Option[Ast.TypeDef] =
    getSymbolType(nodeId) match {
      case Some(t) =>
        t match {
          case integer: Type.Integer => Some(Ast.TypeDefInteger())
          case primitive: Type.Primitive => ???
          case s: Type.String => Some(Ast.TypeDefString())
        }
      case None => None
    }
}
