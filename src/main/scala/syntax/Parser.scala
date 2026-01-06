package syntax

import ast.Locations
import util.{FatalCompilerError, Location, SyntaxError}

import java.io.File
import scala.io.Source
import scala.util.Using
import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.{NoPosition, Position, Positional, Reader as ScalaParserReader}

object Parser extends Parsers {
  implicit val ctx: CompilationContext = CompilationContext()

  private var error: Option[Error] = None

  def parseInputFile[T](p: Parser[T])(f: File): util.Result[T] = {
    error = None
    Metadata.file = f
    Using.resource(Source.fromFile(f)) { source =>
      parseTokens(p)(new Lexer.Scanner(f, source.toArray))
    }
  }

  private def parseTokens[T](p: Parser[T])(scanner: Lexer.Scanner): util.Result[T] = {
    val tokens = scanner.tokenize() match {
      case Left(errors) => return Left(errors)
      case Right(t) => t
    }
    val reader = new TokenReader(tokens)

    parseInputs(p)(reader.asInstanceOf[Input]) match {
      case Success(result, next) => Right(result)
      case NoSuccess(msg, next) => Left(List(SyntaxError(Location(Metadata.file, next.pos), msg)))
      case _ => Left(List(FatalCompilerError()))
    }
  }

  private def parseInputs[T](p: Parser[T]): Parser[T] = (inputs: Input) => {
    val r = p(inputs)
    r match {
      case s@Success(out, in1) =>
        error match {
          case Some(e) => e
          case None => if in1.atEnd then s else Failure("Unexpected token detected", in1)
        }
      case _1 => _1
    }
  }

  def elementSequence: Parser[List[ast.AstNode[_]]] = rep(eol) ~> repsep(declaration, rep1(eol)) <~ rep(eol) ^^ { decls => decls }

  private def declaration: Parser[ast.AstNode[ast.Declaration]] = node {
    printStatement |
    ((Id ~ opt(is ~>! typedef) ~ (assignment ~>! expression)) >> { case id ~ typeDef ~ expr =>
      (but ~> constant ^^ { _ => ast.DecConstant(id, expr, typeDef) }) |
        (opt(but ~> mutable) ^^ { _ => ast.DecMutable(id, expr, typeDef) })
    })
  }

  private def printStatement: Parser[ast.Declaration] =
    print ~> expression ^^ { expr => ast.PrintStatement(expr) }

  private def print = accept("print", { case t: Token.PRINT => t })

  private def constant = accept("constant", { case c: Token.CONSTANT => c })

  private def assignment = accept("=", { case t: Token.ASSIGNMENT => t })

  private def Id: Parser[ast.Ast.Id] = accept("identifier", {
    case Token.IDENTIFIER(s) if !isKeyword(s) => s
    case Token.IDENTIFIER(s) if s == "pi" => s // Allow 'pi' as identifier
  })

  private def isKeyword(s: String): Boolean = {
    Set("plus", "added", "minus", "less", "times", "multiplied", "divided", "over", "modulo", "to", "raised", "squared", "square", "negative", "floor").contains(s)
  }

  private def expression: Parser[ast.AstNode[ast.Expression]] = arithmetic

  private def arithmetic: Parser[ast.AstNode[ast.Expression]] =
    chainl1(term,
      (plus | addedTo | minusBinary | less) ^^ { op => (left: ast.AstNode[ast.Expression], right: ast.AstNode[ast.Expression]) =>
        val node = ast.BinaryExpression(left, right, op)
        val n = ast.AstNode.createNode(node)
        val loc = ast.Locations.get(left.id)
        ast.Locations.put(n.id, loc)
        n
      }
    )

  private def term: Parser[ast.AstNode[ast.Expression]] =
    chainl1(factor,
      (times | multipliedBy | dividedBy | over | modulo | floorDividedBy) ^^ { op => (left: ast.AstNode[ast.Expression], right: ast.AstNode[ast.Expression]) =>
        val node = ast.BinaryExpression(left, right, op)
        val n = ast.AstNode.createNode(node)
        val loc = ast.Locations.get(left.id)
        ast.Locations.put(n.id, loc)
        n
      }
    )

  private def factor: Parser[ast.AstNode[ast.Expression]] =
    atom ~ rep(
      ((toThePower | raisedTo) ~ atom) |
        squared
    ) ^^ { case base ~ ops =>
      ops.foldLeft(base) { (left, op) =>
        op match {
          case (powerOp: ast.BinaryOperator) ~ (right: ast.AstNode[ast.Expression]) =>
             val node = ast.BinaryExpression(left, right, powerOp)
             val n = ast.AstNode.createNode(node)
             ast.Locations.put(n.id, ast.Locations.get(left.id))
             n
          case _ : String => // "squared"
             val two = ast.AstNode.createNode[ast.Expression](ast.IntegerLiteral("2"))
             // two doesn't have location... maybe use left location
             ast.Locations.put(two.id, ast.Locations.get(left.id))
             
             val node = ast.BinaryExpression(left, two, ast.BinaryOperator.Power)
             val n = ast.AstNode.createNode(node)
             ast.Locations.put(n.id, ast.Locations.get(left.id))
             n
        }
      }
    }

  private def atom: Parser[ast.AstNode[ast.Expression]] =
    unary |
    node(literal) |
    lparen ~> expression <~ rparen

  private def unary: Parser[ast.AstNode[ast.Expression]] =
    (squareRootOf | negative | minusUnary) ~ atom ^^ { case op ~ operand =>
      ast.AstNode.createNode(ast.UnaryExpression(operand, op))
    }

  // Operators
  private def ident(s: String): Parser[String] = accept("identifier", { case Token.IDENTIFIER(`s`) => s })

  private def plus = ident("plus") ^^^ ast.BinaryOperator.Add
  private def addedTo = ident("added") ~ ident("to") ^^^ ast.BinaryOperator.Add
  private def minusBinary = (ident("minus") | ident("less")) ^^^ ast.BinaryOperator.Subtract
  private def less = ident("less") ^^^ ast.BinaryOperator.Subtract

  private def times = ident("times") ^^^ ast.BinaryOperator.Multiply
  private def multipliedBy = ident("multiplied") ~ ident("by") ^^^ ast.BinaryOperator.Multiply
  
  private def dividedBy = ident("divided") ~ ident("by") ^^^ ast.BinaryOperator.Divide
  private def over = ident("over") ^^^ ast.BinaryOperator.Divide
  private def floorDividedBy = ident("floor") ~ ident("divided") ~ ident("by") ^^^ ast.BinaryOperator.FloorDivide // TODO: FloorDiv operator? User said "floor divided by" for integer results. Standard Divide is integer? "Floor division: ... for integer results". Usually standard div in languages is integer or float. Let's assume Separate op or same op. User listed it as "Advanced Physics Ops". I should probably add FloorDivide op if I want to be precise, or map to Divide if Divide is integer.
  // User said "Floor division: 'floor divided by' for integer results".
  // Regular "divided by" might be floating point? "distance over time".
  // I will add FloorDivide to BinaryOperator.

  private def modulo = ident("modulo") ^^^ ast.BinaryOperator.Modulo

  private def toThePower = ident("to") ~ ident("the") ~ ident("power") ^^^ ast.BinaryOperator.Power
  private def raisedTo = ident("raised") ~ ident("to") ^^^ ast.BinaryOperator.Power
  private def squared = ident("squared")

  private def squareRootOf = ident("square") ~ ident("root") ~ ident("of") ^^^ ast.UnaryOperator.SquareRoot
  private def negative = ident("negative") ^^^ ast.UnaryOperator.Negate
  private def minusUnary = ident("minus") ^^^ ast.UnaryOperator.Negate

  private def lparen = accept("(", { case Token.LPAREN() => () })
  private def rparen = accept(")", { case Token.RPAREN() => () })


  private def is = accept("is", { case t: Token.IS => t })

  private def literal: Parser[ast.AstLiteral] =
    accept("literal", {
      case Token.INTEGER_LITERAL(value) => ast.IntegerLiteral(value)
      case Token.STRING_LITERAL(value) => ast.StringLiteral(value)
      case Token.INTERPOLATED_STRING_LITERAL(value) => parseInterpolatedString(value)
    }) | rep1(Id) ^^ { identifiers =>
    if (identifiers.length == 1) 
      ast.IdentifierExpression(identifiers.head)
    else 
      ast.StringLiteral(identifiers.mkString(" "))
  }

  private def parseInterpolatedString(rawString: String): ast.InterpolatedString = {
    val segments = scala.collection.mutable.ListBuffer[ast.StringSegment]()
    var i = 0
    val textBuffer = new StringBuilder()

    while (i < rawString.length) {
      if (rawString(i) == '$' && i + 1 < rawString.length) {
        // Flush any accumulated text
        if (textBuffer.nonEmpty) {
          segments += ast.TextSegment(textBuffer.toString())
          textBuffer.clear()
        }

        i += 1 // Skip the '$'
        
        if (rawString(i) == '(') {
          // Complex expression: $(...)
          i += 1 // Skip the '('
          val exprStart = i
          var parenDepth = 1
          
          while (i < rawString.length && parenDepth > 0) {
            if (rawString(i) == '(') parenDepth += 1
            else if (rawString(i) == ')') parenDepth -= 1
            i += 1
          }
          
          val exprString = rawString.substring(exprStart, i - 1)
          // Parse the expression
          val exprTokens = new Lexer.Scanner(Metadata.file, exprString.toArray).tokenize() match {
            case Left(_) => throw new Exception(s"Failed to tokenize expression: $exprString")
            case Right(tokens) => tokens
          }
          
          val reader = new TokenReader(exprTokens)
          parseInputs(expression)(reader.asInstanceOf[Input]) match {
            case Success(expr, _) => segments += ast.ExpressionSegment(expr)
            case _ => throw new Exception(s"Failed to parse expression: $exprString")
          }
        } else {
          // Simple identifier: $identifier
          val identStart = i
          while (i < rawString.length && (rawString(i).isLetterOrDigit || rawString(i) == '_')) {
            i += 1
          }
          val identifier = rawString.substring(identStart, i)
          val identExpr = ast.AstNode.createNode[ast.Expression](ast.IdentifierExpression(identifier))
          segments += ast.ExpressionSegment(identExpr)
        }
      } else {
        textBuffer.append(rawString(i))
        i += 1
      }
    }

    // Flush any remaining text
    if (textBuffer.nonEmpty) {
      segments += ast.TextSegment(textBuffer.toString())
    }

    ast.InterpolatedString(segments.toList)
  }

  private def but = accept("but", { case t: Token.BUT => t })

  private def node[T](p: Parser[T]): Parser[ast.AstNode[T]] =
    final case class Positioned(t: T) extends Positional

    def positionedT: Parser[Positioned] = positioned {
      p ^^ (x => Positioned(x))
    }

    positionedT ^^ {
      case pt@Positioned(t) =>
        val n = ast.AstNode.createNode(t)
        val loc = Location(Metadata.file, pt.pos)
        Locations.put(n.id, loc)
        n
    }

  private def mutable = accept("mutable", { case t: Token.MUTABLE => t })

  private def typedef: Parser[ast.Ast.TypeDef] = accept("type definition", {
    case Token.TYPE_INTEGER() => ast.Ast.TypeDefInteger()
    case Token.TYPE_STRING() => ast.Ast.TypeDefString()
  })

  private def eol = accept("line ending", { case t: Token.EOL => t })

  override def commit[T](p: => Parser[T]): Parser[T] = Parser { in =>
    def setError(e: Error) = {
      error match {
        case None => error = Some(e)
        case _ =>
      }
      e
    }

    val r = p(in)
    r match {
      case s@Success(result, next) => s
      case e: Error => setError(e)
      case Failure(msg, next) => setError(Error(msg, next))
    }
  }

  def Elem: Token.type = Token

  private class TokenReader(tokens: List[Token]) extends ScalaParserReader[Token] {
    override def first: Token = tokens.head

    override def rest: ScalaParserReader[Token] = new TokenReader(tokens.tail)

    override def pos: Position = tokens.headOption.map(_.pos).getOrElse(NoPosition)

    override def atEnd: Boolean = tokens.isEmpty
  }

  private object Metadata {
    var file: File = _
  }
}