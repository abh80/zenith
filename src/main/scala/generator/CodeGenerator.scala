package generator

import analysis.Analyzer
import ast.AstNode
import util.Result

sealed trait Target

object Target {
  case object JavaScript extends Target
}

trait CodeGenerator {
  def target: Target

  def generateCode(nodes: List[AstNode[?]], analyzer: Analyzer): Result[String]
}

object CodeGenerator {
  def target(target: Target): CodeGenerator = target match {
    case Target.JavaScript => new JavaScriptGenerator()
  }
}