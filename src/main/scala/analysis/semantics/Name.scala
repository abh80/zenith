package analysis.semantics

import ast.Ast
import util.CompilerError

import scala.annotation.tailrec

/*
  Qualifier is the full reference of a symbol sequence ex: A.B.C
  Unqualified is the base C which has no meaning by itself, ex. B, C
 */
object Name {
  type Unqualified = String

  case class Qualified(qualifier: List[Unqualified], base: Unqualified) {
    override def toString: String =
      def fl(s1: String, s2: String) = s1 ++ "." ++ s2

      def convertQualifiersToString = qualifier match {
        case None => ""
        case head :: tail => tail.fold(head)(f) ++ "."
      }

      convertQualifiersToString ++ base

    def trimScopePrefix(currentScope: List[Ast.Id]): Qualified =
      @tailrec
      def helper(prefixList: List[String], resultList: List[String]): Qualified = {
        val result = Qualifier.fromIdentifierList(resultList)
        (prefixList, resultList) match {
          case (head1 :: tail1, head2 :: tail2) => if head1 == head2 then helper(tail1, tail2) else result
          case _ => result
        }
      }

      helper(currentScope, this.toIdentifierList)

    def toIdentifierList: List[Unqualified] = qualifier :+ base
  }

  private object Qualifier {
    def fromIdentifierList(ids: List[Ast.Id]): Qualified =
      ids.reverse match {
        case head :: tail => Qualified(tail.reverse, head)
        case _ => throw CompilerError("Identifier list was empty")
      }
  }
}
