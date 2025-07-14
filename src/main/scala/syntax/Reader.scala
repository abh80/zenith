package syntax

import util.CharsUtil

abstract class Reader extends CharsUtil {
  thisArg =>
  val content: Array[Char]
  var charCurrentOffset: Int = startFrom
  var charLastOffset: Int = startFrom
  var lineStartOffset: Int = startFrom
  var lineNumber: Int = 0

  var ch: Char = _

  def isAtEnd: Boolean = charCurrentOffset >= content.length

  def startFrom: Int = 0

  def lookaheadChar(): Char = lookaheadReader().getc()

  def getc(): Char =
    advance()
    ch

  final def advance(): Unit = {
    val index = charCurrentOffset
    charLastOffset = index
    charCurrentOffset += 1
    if (index >= content.length) ch = SU
    else {
      ch = content(index)
      if ch < ' ' then {
        skipCR()
        checkLineEnding()
      }
    }
  }

  private def checkLineEnding(): Unit = {
    if ch == LF || ch == FF then {
      lineStartOffset = charCurrentOffset
      lineNumber += 1
    }
  }

  private def skipCR(): Unit = {
    if ch == CR then
      if charCurrentOffset < content.length && content(charCurrentOffset) == LF then {
        charCurrentOffset += 1
        ch = LF
      }
  }

  def lookaheadReader(): CharArrayLookaheadReader = new CharArrayLookaheadReader

  class CharArrayLookaheadReader extends Reader {
    val content: Array[Char] = thisArg.content
    charCurrentOffset = thisArg.charCurrentOffset
    ch = thisArg.ch
  }
}
