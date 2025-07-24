package generator.emitters

trait Emitter {
  def emitStringLiteral(value: String): String

  def emitIntegerLiteral(value: String): String

  def emitIdentifier(name: String): String

  def emitComment(comment: String): String
}