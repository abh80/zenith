package syntax

import analysis.Analyzer
import ast.AstNode
import generator.{CodeGenerator, Target}
import util.{Error, Problem, Result, Warning}

class CompilationContext {

  private var nextId: AstNode.Id = 0
  private var errors: Vector[Error] = Vector.empty
  private var warnings: Vector[Warning] = Vector.empty

  def getNextId: AstNode.Id = {
    val id = nextId
    nextId += 1
    id
  }

  def reportError(err: Error): Unit = {
    errors = errors :+ err
  }

  def reportWarning(warn: Warning): Unit = {
    warnings = warnings :+ warn
  }

  /** Prints all errors and warnings */
  def print(): Unit = {
    if (hasWarnings) {
      println("Warnings:")
      warnings.foreach(_.print())
    }
    if (hasErrors) {
      println("Errors:")
      errors.foreach(_.print())
    }
  }

  def generateCode(nodes: List[AstNode[_]], analyzer: Analyzer, target: Target): Result[String] =
    val generator = CodeGenerator.target(target)
    generator.generateCode(nodes, analyzer)

  /** Checks if there are any warnings */
  def hasWarnings: Boolean = warnings.nonEmpty

  /** Returns the compilation result */
  def result[T](value: => T): Either[List[Problem], T] = {
    if (hasErrors) Left(getErrors)
    else Right(value)
  }

  /** Checks if there are any errors */
  def hasErrors: Boolean = errors.nonEmpty

  /** Gets all accumulated errors */
  def getErrors: List[Problem] = errors.toList

  /** Gets all accumulated warnings */
  def getWarnings: List[Problem] = warnings.toList
}