package generator.emitters

class JavaScriptEmitter extends Emitter {

  override def emitStringLiteral(value: String): String =
    val escaped = value
      .replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\n", "\\n")
      .replace("\r", "\\r")
      .replace("\t", "\\t")
    s"\"$escaped\""

  override def emitIntegerLiteral(value: String): String = value

  override def emitIdentifier(name: String): String = name

  override def emitComment(comment: String): String = s"// $comment"
}
