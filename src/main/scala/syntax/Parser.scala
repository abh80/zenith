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
    println(tokens)
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
    (Id ~ opt(is ~>! typedef) ~ (assignment ~>! node(literal))) >> { case id ~ typeDef ~ lit =>
      (but ~> constant ^^ { _ => ast.DecConstant(id, lit, typeDef) }) |
        (opt(but ~> mutable) ^^ { _ => ast.DecMutable(id, lit, typeDef) })
    }
  }

  private def constant = accept("constant", { case c: Token.CONSTANT => c })

  private def assignment = accept("=", { case t: Token.ASSIGNMENT => t })

  private def Id: Parser[ast.Ast.Id] = accept("identifier", { case Token.IDENTIFIER(s) => s })

  private def is = accept("is", { case t: Token.IS => t })

  private def literal: Parser[ast.AstLiteral] =
    accept("literal", {
      case Token.INTEGER_LITERAL(value) => ast.IntegerLiteral(value)
      case Token.STRING_LITERAL(value) => ast.StringLiteral(value)
    }) | rep1(accept("identifier", { case Token.IDENTIFIER(ident) => ident })) ^^ { identifiers =>
    if (identifiers.length == 1) 
      ast.IdentifierExpression(identifiers.head)
    else 
      ast.StringLiteral(identifiers.mkString(" "))
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