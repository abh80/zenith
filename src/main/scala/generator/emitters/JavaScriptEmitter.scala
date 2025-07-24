package generator.emitters

class JavaScriptEmitter extends Emitter {

  override def emitStringLiteral(value: String): String = ???

  override def emitIntegerLiteral(value: String): String = value

  override def emitIdentifier(name: String): String = ???
}
