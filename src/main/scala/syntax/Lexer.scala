package syntax

import syntax.tokenId.*
import util.{InvalidToken, Location, Problem}

import java.io.File
import scala.annotation.{switch, tailrec}
import scala.collection.mutable.ArrayBuffer
import scala.util.parsing.input.Position

object Lexer {
  lazy val reservedWords: Set[String] = keywords.keySet
  private val keywords: Map[String, tokenId] = Map(
    ("but", BUT),
    ("final", CONSTANT),
    ("constant", CONSTANT),
    ("mutable", MUTABLE),
    ("print", PRINT),
    ("is", IS),
    ("string", TYPE_STRING),
    ("text", TYPE_STRING),
    ("number", TYPE_INTEGER)
  )

  class Scanner(file: File, val content: Array[Char])(using ctx: CompilationContext) extends Reader with TokenInfo with Iterator[Token] {
    private val lines = new String(content).split("\n", -1)
    private val literalBuffer = new StringBuffer()
    var token: tokenId = EOF

    override def hasNext: Boolean = !isAtEnd

    override def next(): Token = {
      if token == EOF then advance()
      loadToken()

      val o: Token = token match {
        case EOF => return Token.EOF
        case BUT => Token.BUT()
        case CONSTANT => Token.CONSTANT()
        case MUTABLE => Token.MUTABLE()
        case PRINT => Token.PRINT()
        case INTEGER_LITERAL => Token.INTEGER_LITERAL(strVal)
        case STRING_LITERAL => Token.STRING_LITERAL(strVal)
        case INTERPOLATED_STRING_LITERAL => Token.INTERPOLATED_STRING_LITERAL(strVal)
        case IDENTIFIER => Token.IDENTIFIER(strVal)
        case ASSIGNMENT => Token.ASSIGNMENT()
        case EOL => Token.EOL()
        case IS => Token.IS()
        case TYPE_STRING => Token.TYPE_STRING()
        case TYPE_INTEGER => Token.TYPE_INTEGER()
        case LPAREN => Token.LPAREN()
        case RPAREN => Token.RPAREN()
      }

      o.setPos(pos())
    }

    def error(msg: String): Unit =
      ctx.reportError(InvalidToken(Location(file, pos()), msg))

    def tokenize(): Either[List[Problem], List[Token]] = {
      val tokens = ArrayBuffer[Token]()
      var current = next()

      while (current != Token.EOF) {
        tokens += current
        current = next()
      }

      ctx.result(tokens.toList)
    }

    private def pos() = {
      new TokenPosition(
        line + 1, offset - lineOffset + 1, lines(Math.max(0, Math.min(line, lines.length - 1)))
      )
    }

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

        case '=' =>
          advance()
          token = ASSIGNMENT
          skipToNextToken()

        case LF =>
          skipToNextToken()
          token = EOL

        case '"' =>
          advance()
          loadString()
          
        case '(' =>
          advance()
          token = LPAREN
          
        case ')' =>
          advance()
          token = RPAREN

        case invalidToken =>
          error(s"Invalid token: '$invalidToken'")
          advance()
          loadToken()
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

    // ignore all whitespaces and newlines
    @tailrec
    private def skipToNextToken(): Unit = {
      (ch: @switch) match {
        case LF | ' ' =>
          advance()
          skipToNextToken()
        case _ =>
      }
    }

    private def loadString(): Unit = {
      loadStringWithInterpolation(hasInterpolation = false)
    }

    @tailrec
    private def loadStringWithInterpolation(hasInterpolation: Boolean): Unit = {
      (ch: @switch) match {
        case '"' =>
          advance()
          token = if (hasInterpolation) tokenId.INTERPOLATED_STRING_LITERAL else tokenId.STRING_LITERAL
          flushLiteralBufferToStrVal()

        case SU | LF | CR =>
          error("Unterminated string literal")
          token = if (hasInterpolation) tokenId.INTERPOLATED_STRING_LITERAL else tokenId.STRING_LITERAL
          flushLiteralBufferToStrVal()

        case '\\' =>
          advance()
          ch match {
            case 'n' => putCharInBuffer('\n')
            case 't' => putCharInBuffer('\t')
            case 'r' => putCharInBuffer('\r')
            case '\\' => putCharInBuffer('\\')
            case '"' => putCharInBuffer('"')
            case _ => error(s"Invalid escape sequence '\\$ch'")
          }
          advance()
          loadStringWithInterpolation(hasInterpolation)

        case '$' =>
          putCharInBuffer(ch)
          advance()
          loadStringWithInterpolation(hasInterpolation = true)

        case _ =>
          putCharInBuffer(ch)
          advance()
          loadStringWithInterpolation(hasInterpolation)
      }
    }
  }

  private class TokenPosition(val line: Int, val column: Int, lineStr: String) extends Position {
    override def lineContents: String = lineStr
  }
}
