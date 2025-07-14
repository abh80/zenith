package syntax
import scala.util.parsing.input.Positional

sealed trait Token extends Positional

trait TokenInfo {
  var token: tokenId

  /**
   * Represents the line number in the source code where the first character of the current token appears.
   * Useful for error reporting or debugging (e.g., "Error at line 5").
   */
  var line: Int = 0

  /**
   * Stores the character offset (position) of the first character of the current token in the source code.
   * This is typically measured from the start of the file or input stream.
   */
  var offset : Int = 0

  /**
   * Represents the offset of the character immediately following the token that preceded the current one.
   * This helps track the position where the previous token ended, which can be useful for determining token boundaries or gaps (e.g., whitespace).
   */
  var lastOffset: Int = 0

  /**
   * Stores the offset of the newline character immediately preceding the current token.
   * If the token is not preceded by a newline (e.g., it’s on the same line as the previous token).
   */
  var lineOffset: Int = 0

  /**
   * Indicates whether the next identifier should be treated as a reserved word (keyword) or not
   */
  var reservedWord: Boolean = false

  /**
   * Represents the numerical base of a number token (e.g., 10 for decimal, 16 for hexadecimal, 8 for octal)
   */
  var base: Int = 0

  /**
   * Stores the string value of a literal token, such as the content of a string literal
   */
  var strVal: String = ""

  def copyFrom(other: TokenInfo): Unit = {
    this.token = other.token
    this.offset = other.offset
    this.lastOffset = other.lastOffset
    this.lineOffset = other.lineOffset
    this.base = other.base
    this.strVal = other.strVal
  }
}

object Token {
  case object EOF extends Token
  final case class CONSTANT() extends Token
  final case class IDENTIFIER(name: String) extends Token
  final case class STRING_LITERAL(value: String) extends Token
  final case class INTEGER_LITERAL(value: TypeInteger)
}

enum tokenId {
  // eof
  case EOF

  // identifier
  case IDENTIFIER

  // keywords
  case CONSTANT
  case MUTABLE
  case BUT
  case INTEGER_LITERAL
}

sealed trait TypeInteger