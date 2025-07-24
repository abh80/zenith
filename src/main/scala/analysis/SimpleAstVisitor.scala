package analysis

import ast.{AstNode, AstVisitor}
import util.{Problem, Result}

import scala.collection.mutable

class SimpleAstVisitor(protected val nodes: List[AstNode[?]]) extends AstVisitor {
  def foldWithRecovery[A](initial: A)(f: (A, AstNode[?]) => Result[A]): Result[A] =
    val problems = mutable.ListBuffer[Problem]()
    val result = nodes.foldLeft(initial) { (acc, node) =>
      f(acc, node) match {
        case Left(nodeProblems) =>
          problems ++= nodeProblems
          acc
        case Right(newacc) => newacc
      }
    }
    if problems.isEmpty then Right(result) else Left(problems.toList)

}
