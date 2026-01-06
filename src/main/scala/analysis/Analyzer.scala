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
                     usedSymbolSet: ListSet[Symbol] = ListSet(),
                     printStatements: List[AstNode[PrintStatement]] = List()
                   ) {
  private type Node = AstNode[?]

  def analyzeNode(node: Node): Result[Analyzer] = {
    node.data match {
      case decConstant: DecConstant => new DeclarationAnalysis(this, node.asInstanceOf[AstNode[Declaration]]).analyzeConstantDeclaration(decConstant, node.id)
      case decMutable: DecMutable => new DeclarationAnalysis(this, node.asInstanceOf[AstNode[Declaration]]).analyzeMutableDeclaration(decMutable, node.id)
      case printStmt: PrintStatement => new DeclarationAnalysis(this, node.asInstanceOf[AstNode[Declaration]]).analyzePrintStatement(printStmt, node.id)
    }
  }
}

object Analyzer {
  def analyze(ast: List[AstNode[?]]): Result[Analyzer] = {
    val visitor = new SimpleAstVisitor(ast)
    val initialAnalyzer = Analyzer(
      level = 0,
      currentScope = Scope.createGlobal()
    )

    val res = visitor.foldWithRecovery(Result.success(initialAnalyzer)) { (result, node) =>
      result match {
        case Left(problems) => Right(Left(problems))
        case Right(analyzer) =>
          analyzer.analyzeNode(node) match {
            case Left(problems) => Right(Left(problems))
            case Right(newAnalyzer) => Right(Right(newAnalyzer))
          }
      }
    }
    res.flatten
  }
}
