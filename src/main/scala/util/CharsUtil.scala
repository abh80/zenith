package util

class CharsUtil {
  inline val LF = '\u000A'
  inline val FF = '\u000C'
  inline val CR = '\u000D'
  inline val SU = '\u001A'

  def isWhitespace(c: Char): Boolean = c.isWhitespace

  def isIdentifierStart(c: Char): Boolean = isLetter(c) || c == '_'

  def isLetter(c: Char): Boolean = c.isLetter

  def isIdentifierPart(c: Char): Boolean = isLetter(c) || isDigit(c) || c == '_'

  def isDigit(c: Char): Boolean = c.isDigit
}