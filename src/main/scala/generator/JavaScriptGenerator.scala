package generator

import analysis.Analyzer
import analysis.semantics.Symbol.{ConstantSymbol, MutableSymbol}
import analysis.semantics.{Symbol, Type, Value}
import generator.Target.JavaScript
import generator.emitters.JavaScriptEmitter
import util.Result

class JavaScriptGenerator extends CodeGenerator {

  private val emitter = new JavaScriptEmitter()

  override def target: Target = JavaScript

  override def generateCode(analyzer: Analyzer): Result[String] = {
    val context = GeneratorContext(analyzer, analyzer.currentScope)
    val symbolsToProcess = analyzer.usedSymbolSet

    val res = symbolsToProcess.toList.map { symbol =>
      generateDeclarationForSymbol(symbol, context)
    }

    val (err, success) = res.partitionMap(identity)
    if err.nonEmpty then Left(err.flatten)
    else Right(success.mkString("\n\n"))
  }

  private def generateDeclarationForSymbol(symbol: Symbol, ctx: GeneratorContext): Result[String] = {
    symbol match {
      case sym: ConstantSymbol => Right(generateConstantDeclaration(sym, ctx))
      case sym: MutableSymbol => Right(generateMutableDeclaration(sym, ctx))
    }
  }

  private def generateConstantDeclaration(symbol: ConstantSymbol, ctx: GeneratorContext): String =
    generateDeclaration(symbol, ctx, "const")

  private def generateMutableDeclaration(symbol: MutableSymbol, ctx: GeneratorContext): String =
    generateDeclaration(symbol, ctx, "let")


  private def generateTypeDefComment(typeDef: Type, ctx: GeneratorContext): String = {
    val comment = typeDef match {
      case integer: Type.Integer => "@type {number}"
      case primitive: Type.Primitive => ???
      case str: Type.String => "@type {string}"
      case reference: Type.IdentifierReference => "" // never needed since it will always be resolved to a native type
    }
    emitter.emitComment(comment)
  }

  private def generateExpression(value: Value, context: GeneratorContext): String =
    value match {
      case Value.Integer(value) => emitter.emitIntegerLiteral(value.toString)
      case Value.String(value) => emitter.emitStringLiteral(value)
      case Value.IdentifierReference(name, _) => emitter.emitIdentifier(name)
    }

  private def generateDeclaration(symbol: Symbol, ctx: GeneratorContext, keyword: String): String = {
    val jsVal = generateExpression(ctx.getSymbolValue(symbol.nodeId).get, ctx)
    val typeDef = ctx.getSymbolType(symbol.nodeId).get
    val typeDefComment = generateTypeDefComment(typeDef, ctx)

    val comment = if (typeDefComment.nonEmpty) s"$typeDefComment\n${ctx.indent}" else ""
    s"${comment}$keyword ${symbol.name} = $jsVal;"
  }
}