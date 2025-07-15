package util

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

object Problem {
  private def printErr(locOpt: Option[Location])(msg: String): Unit = {
    if locOpt.isDefined then System.err.println(locOpt.get.toString)
    System.err.println(f"error: ${msg}")
  }
}
