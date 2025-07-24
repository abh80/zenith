package generator

import analysis.Analyzer
import ast.*
import generator.Target.JavaScript
import generator.emitters.JavaScriptEmitter
import util.{Result, UnsupportedNodeTypeForCodeGeneratorContext}

class JavaScriptGenerator extends CodeGenerator {

  private val emitter = new JavaScriptEmitter()

  override def target: Target = JavaScript

  override def generateCode(nodes: List[AstNode[_]], analyzer: Analyzer): Result[String] =
    val context = GeneratorContext(analyzer, analyzer.currentScope)
    val res = nodes.map(generateNode(_, context))

    val (err, success) = res.partitionMap(identity)
    if err.nonEmpty then Left(err.flatten)
    else Right(success.mkString("\n\n"))

  private def generateNode(node: AstNode[_], ctx: GeneratorContext): Result[String] =
    node.data match {
      case decl: Declaration => generateDeclaration(node.asInstanceOf[AstNode[Declaration]], ctx)
      case _ => Left(List(UnsupportedNodeTypeForCodeGeneratorContext(node)))
    }

  private def generateDeclaration(declNode: AstNode[Declaration], context: GeneratorContext): Result[String] =
    declNode.data match {
      case DecConstant(name, value, typeDef) => generateConstantDeclaration(name, value, typeDef, context)
      case DecMutable(name, value, typeDef) => generateMutableDeclaration(name, value, typeDef, context)
    }

  private def generateConstantDeclaration(name: Ast.Id, value: AstNode[AstLiteral], maybeTypeDef: Option[Ast.TypeDef], ctx: GeneratorContext): Result[String] =
    for {
      jsVal <- generateExpression(value, ctx)
    } yield s"const $name = $jsVal;"

  private def generateMutableDeclaration(name: Ast.Id, value: AstNode[AstLiteral], maybeTypeDef: Option[Ast.TypeDef], ctx: GeneratorContext): Result[String] =
    for {
      jsVal <- generateExpression(value, ctx)
    } yield s"let $name = $jsVal;"

  private def generateExpression(node: AstNode[AstLiteral], context: GeneratorContext): Result[String] =
    node.data match {
      case StringLiteral(value) => Right(emitter.emitStringLiteral(value))
      case IntegerLiteral(value) => Right(emitter.emitIntegerLiteral(value))
      case IdentifierExpression(value) => ???
    }
}
