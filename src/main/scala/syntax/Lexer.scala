package syntax

import syntax.tokenId.{EOF, IDENTIFIER}

import java.io.File
import scala.annotation.{switch, tailrec}
import scala.util.parsing.input.Position

object Lexer {
  val reservedWords: Set[String] = keywords.keySet
  private val keywords: Map[String, tokenId] = Map(
    ("but", tokenId.BUT),
    ("final", tokenId.CONSTANT),
    ("constant", tokenId.CONSTANT),
    ("mutable", tokenId.MUTABLE)
  )

  class TokenPosition(val line: Int, val column: Int, lineStr: String) extends Position {
    override def lineContents: String = lineStr
  }

  class Scanner(file: File, val content: Array[Char]) extends Reader with TokenInfo with Iterator[Token] {
    val lines = new String(content).split("\n", -1)
    private val literalBuffer = new StringBuffer()
    var token: tokenId = EOF

    override def hasNext: Boolean = !isAtEnd

    override def next(): Token = {
      if token == EOF then advance()
      loadToken()
      ???
    }

    def error(msg: String): Unit = ???

    private def putCharInBuffer(c: Char): Unit = literalBuffer.append(c)

    @tailrec
    private def loadToken(): Unit = {
      offset = charCurrentOffset - 1
      lineOffset = if (lastOffset < lineStartOffset) lineStartOffset else 0
      reservedWord = false
      line = lineNumber
      ch match {
        case SU => token = EOF
        case ' ' | FF =>
          advance()
          loadToken()

        case id if isIdentifierStart(id) =>
          putCharInBuffer(ch)
          advance()
          loadRemainingIdentifier()

        case '0' =>
          advance()
          putCharInBuffer('0')
          ch match {
            case 'x' | 'X' =>
              base = 16
              putCharInBuffer('x')
              advance()
            case _ =>
              base = 10
          }
          loadRemainingNumber()

        case d if isDigit(ch) =>
          base = 10
          loadRemainingNumber()
      }
    }

    @tailrec
    private def loadRemainingIdentifier(): Unit = {
      (ch: @switch) match {
        case id if isIdentifierPart(id) =>
          putCharInBuffer(ch)
          advance()
          loadRemainingIdentifier()
        case _ => finishLoadingIdentifier()
      }
    }

    private def flushLiteralBufferToStrVal(): Unit =
      strVal = literalBuffer.toString
      literalBuffer.setLength(0)

    private def finishLoadingIdentifier(): Unit = {
      flushLiteralBufferToStrVal()
      if reservedWord then token = IDENTIFIER
      else
        token = keywords.get(strVal) match {
          case Some(key) => key
          case None => IDENTIFIER
        }
    }

    private def loadRemainingNumber(): Unit = {
      while (digitToInt(ch, base) >= 0) {
        putCharInBuffer(ch)
        advance()
      }
      token = tokenId.INTEGER_LITERAL
      flushLiteralBufferToStrVal()
    }
  }
}
