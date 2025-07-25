package util

import analysis.semantics.Type
import ast.AstNode

sealed trait Problem {
  def print(): Unit = {
    this match {
      case InvalidToken(loc, msg) =>
        Problem.printErr(Some(loc))(s"invalid token: $msg")
      case SyntaxError(loc, msg) =>
        Problem.printErr(Some(loc))(s"syntax error: $msg")
      case UnknownTypeDefinition(loc, msg) =>
        Problem.printErr(Some(loc))(s"unknown type definition: $msg")
      case FatalCompilerError(msg) =>
        Problem.printErr(None)(msg)
      case TypeMismatch(loc, type1, type2) =>
        Problem.printErr(Some(loc))(s"type mismatch: expected $type1 but found $type2 instead")
      case DuplicateSymbolDeclaration(loc, name) =>
        Problem.printErr(Some(loc))(s"duplicate symbol declaration: $name")
      case UnsupportedNodeTypeForCodeGeneratorContext(node) =>
        Problem.printErr(None)(s"unsupported node type for code generator: ${node.getClass.getSimpleName}")
      case UndeclaredIdentifierReferenced(loc, name) => Problem.printErr(Some(loc))(s"undeclared identifier referenced: $name")
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

final case class UndeclaredIdentifierReferenced(loc: Location, name: String) extends Error

object Problem {
  private def printErr(locOpt: Option[Location])(msg: String): Unit = {
    if locOpt.isDefined then System.err.println(locOpt.get.toString)
    System.err.println(f"error: ${msg}")
  }
}

final case class CompilerError(msg: String) extends Exception(msg)