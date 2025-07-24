package util

import analysis.semantics.Type
import ast.AstNode

sealed trait Problem {
  def print(): Unit = {
    this match {
      case InvalidToken(loc, msg) =>
        Problem.printErr(Some(loc))(s"invalid token: $msg")
      case _ =>
    }
  }
}

abstract class Warning extends Problem

abstract class Error extends Problem

final case class InvalidToken(loc: Location, msg: String) extends Error

final case class SyntaxError(loc: Location, msg: String) extends Error

final case class UnknownTypeDefinition(loc: Location, msg: String) extends Error

final case class FatalCompilerError(msg: String = "Fatal compiler error has occurred please contact the maintainer") extends Error

final case class TypeMismatch(loc: Location, type1: Type, type2: Type) extends Error

final case class DuplicateSymbolDeclaration(loc: Location, name: String) extends Error

final case class UnsupportedNodeTypeForCodeGeneratorContext(node: AstNode[_]) extends Error

object Problem {
  private def printErr(locOpt: Option[Location])(msg: String): Unit = {
    if locOpt.isDefined then System.err.println(locOpt.get.toString)
    System.err.println(f"error: ${msg}")
  }
}

final case class CompilerError(msg: String) extends Exception(msg)