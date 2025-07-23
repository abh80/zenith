import analysis.semantics.Scope
import analysis.{Analyzer, SimpleAstVisitor}
import syntax.Parser
import util.Result

import java.io.File

object Test {
  def main(args: Array[String]): Unit = {
    val p = Parser.parseInputFile(Parser.elementSequence)(new File("test.z"))
    println(p)
    p match {
      case Left(value) => ???
      case Right(nodes) =>
        val visitor = new SimpleAstVisitor(nodes)
        val globalScope = Scope.createGlobal()
        val initialAnalyzer = Analyzer(
          level = 0,
          currentScope = globalScope
        )
        visitor.foldLeft(Result.success(initialAnalyzer)) { (result, node) =>
          println(s"Analyzing node: ${node.id}")
          result.flatMap { a =>
            println(a)
            a.analyzeNode(node)
          }
        }
    }
  }
}