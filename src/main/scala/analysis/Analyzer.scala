package analysis

import analysis.analyzers.DeclarationAnalysis
import analysis.semantics.*
import ast.*
import util.Result

import scala.collection.immutable.ListSet

case class Analyzer(
                     level: Int,
                     currentScope: Scope,
                     scopeNameList: List[Name.Unqualified] = List(),
                     parentSymbol: Option[Symbol] = None,
                     parentSymbolsMap: Map[Symbol, Symbol] = Map(),
                     symbolScopeMap: Map[Symbol, Scope] = Map(),
                     typeMap: Map[AstNode.Id, Type] = Map(),
                     valueMap: Map[AstNode.Id, Value] = Map(),
                     usedSymbolSet: ListSet[Symbol] = ListSet()
                   ) {
  private type Node = AstNode[?]

  def analyzeNode(node: Node): Result[Analyzer] = {
    node.data match {
      case decConstant: DecConstant => new DeclarationAnalysis(this, node.asInstanceOf[AstNode[Declaration]]).analyzeConstantDeclaration(decConstant, node.id)
      case decMutable: DecMutable => new DeclarationAnalysis(this, node.asInstanceOf[AstNode[Declaration]]).analyzeMutableDeclaration(decMutable, node.id)
    }
  }
}
