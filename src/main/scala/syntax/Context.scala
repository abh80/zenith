package syntax

import util.{Error, Warning, Problem}

class Context {
  private var errors: Vector[Error] = Vector.empty
  private var warnings: Vector[Warning] = Vector.empty

  def reportError(err: Error): Unit = {
    errors = errors :+ err
  }

  def reportWarning(warn: Warning): Unit = {
    warnings = warnings :+ warn
  }

  /** Checks if there are any errors */
  def hasErrors: Boolean = errors.nonEmpty

  /** Checks if there are any warnings */
  def hasWarnings: Boolean = warnings.nonEmpty

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

  /** Returns the compilation result */
  def result[T](value: => T): Either[List[Problem], T] = {
    if (hasErrors) Left(getErrors)
    else Right(value)
  }

  /** Gets all accumulated errors */
  def getErrors: List[Problem] = errors.toList

  /** Gets all accumulated warnings */
  def getWarnings: List[Problem] = warnings.toList
}