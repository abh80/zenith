package generator

import analysis.Analyzer
import analysis.semantics.{Symbol, Type, Value}
import ast.{AstNode, PrintStatement, Declaration, DecConstant, DecMutable, IfStatement}
import generator.Target.JavaScript
import generator.emitters.JavaScriptEmitter
import util.Result

class JavaScriptGenerator extends CodeGenerator {

  private val emitter = new JavaScriptEmitter()

  override def target: Target = JavaScript

  override def generateCode(analyzer: Analyzer): Result[String] = {
    val context = GeneratorContext(analyzer, analyzer.currentScope)
    
    val statements = analyzer.executionLog.map { node =>
      generateStatement(node, context)
    }

    val (err, success) = statements.partitionMap(identity)
    if err.nonEmpty then Left(err.flatten)
    else Right(success.mkString("\n\n"))
  }

  private def generateStatement(node: AstNode[Declaration], ctx: GeneratorContext): Result[String] = {
    node.data match {
      case dec: DecConstant =>
        Right(generateConstantDeclaration(Symbol.ConstantSymbol(node.asInstanceOf[AstNode[DecConstant]]), ctx))
      case dec: DecMutable =>
        Right(generateMutableDeclaration(Symbol.MutableSymbol(node.asInstanceOf[AstNode[DecMutable]]), ctx))
      case print: PrintStatement =>
        generatePrintStatement(node.asInstanceOf[AstNode[PrintStatement]], ctx)
      case ifStmt: IfStatement =>
        generateIfStatement(ifStmt, ctx)
    }
  }

  private def generateIfStatement(stmt: IfStatement, ctx: GeneratorContext): Result[String] = {
     // Retrieve condition value from map (it was stored via analyzeIfStatement)
     // But wait, key in map is `stmt.condition.id`.
     val condVal = ctx.analyzer.valueMap(stmt.condition.id)
     val condJs = generateExpression(condVal, ctx)
     
     // Generate Then Block
     val thenPart = stmt.thenBlock.map(node => generateStatement(node, ctx)).partitionMap(identity) match {
       case (errs, _) if errs.nonEmpty => return Left(errs.flatten)
       case (_, stmts) => stmts.mkString("\n" + ctx.indent) // Indentation handling?
     }
     
     val elsePart = stmt.elseBlock.map { block =>
       block.map(node => generateStatement(node, ctx)).partitionMap(identity) match {
         case (errs, _) if errs.nonEmpty => return Left(errs.flatten)
         case (_, stmts) => s" else {\n$stmts\n}"
       }
     }.getOrElse("")
     
     // Simple block formatting
     Right(s"if ($condJs) {\n$thenPart\n}$elsePart")
  }

  private def generatePrintStatement(node: AstNode[PrintStatement], ctx: GeneratorContext): Result[String] = {
    val value = ctx.getSymbolValue(node.id).get
    val jsExpr = generateExpression(value, ctx)
    Right(s"console.log($jsExpr);")
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
      case float: Type.Float => "@type {number}"
      case primitive: Type.Primitive => ???
      case str: Type.String => "@type {string}"
      case reference: Type.IdentifierReference => "" // never needed since it will always be resolved to a native type
    }
    emitter.emitComment(comment)
  }

  private def generateExpression(value: Value, context: GeneratorContext): String =
    value match {
      case Value.Integer(value) => emitter.emitIntegerLiteral(value.toString)
      case Value.Float(value) => value.toString
      case Value.String(value) => emitter.emitStringLiteral(value)
      case Value.IdentifierReference(name, _) => emitter.emitIdentifier(name)
      case Value.BinaryOp(left, right, op) =>
        val l = generateExpression(left, context)
        val r = generateExpression(right, context)
        val opStr = op match {
          case ast.BinaryOperator.Add => "+"
          case ast.BinaryOperator.Subtract => "-"
          case ast.BinaryOperator.Multiply => "*"
          case ast.BinaryOperator.Divide => "/"
          case ast.BinaryOperator.FloorDivide => "/" // JS division is float, but if used in logic, verify user intent. User said "floor divided by... for integer results". JS `Math.floor(x/y)`.
          case ast.BinaryOperator.Modulo => "%"
          case ast.BinaryOperator.Power => "**"
          case ast.BinaryOperator.GreaterThan => ">"
          case ast.BinaryOperator.LessThan => "<"
          case ast.BinaryOperator.EqualTo => "==="
        }
        if (op == ast.BinaryOperator.FloorDivide) s"Math.floor($l / $r)"
        else s"($l $opStr $r)"
      case Value.UnaryOp(operand, op) =>
        val o = generateExpression(operand, context)
        op match {
          case ast.UnaryOperator.Negate => s"(-$o)"
          case ast.UnaryOperator.SquareRoot => s"Math.sqrt($o)"
        }
      case Value.InterpolatedString(segments) =>
        generateInterpolatedString(segments, context)
    }

  private def generateInterpolatedString(segments: List[Value.InterpolatedSegment], context: GeneratorContext): String = {
    // Use JavaScript template literals
    val parts = segments.map {
      case Value.TextSegment(text) => text
      case Value.ExprSegment(value) => s"$${${generateExpression(value, context)}}"
    }
    s"`${parts.mkString("")}`"
  }

  private def generateDeclaration(symbol: Symbol, ctx: GeneratorContext, keyword: String): String = {
    val jsVal = generateExpression(ctx.getSymbolValue(symbol.nodeId).get, ctx)
    val typeDef = ctx.getSymbolType(symbol.nodeId).get
    val typeDefComment = generateTypeDefComment(typeDef, ctx)

    val comment = if (typeDefComment.nonEmpty) s"$typeDefComment\n${ctx.indent}" else ""
    s"${comment}$keyword ${symbol.name} = $jsVal;"
  }
}