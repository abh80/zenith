package syntax

import ast.Locations
import util.{FatalCompilerError, Location, SyntaxError}

import java.io.File
import scala.io.Source
import scala.util.Using
import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.{NoPosition, Position, Positional, Reader as ScalaParserReader}

object Parser extends Parsers {

  private var error: Option[Error] = None

  def decConstant: Parser[ast.DecConstant] = Id ~! (assignment ~>! node(literal)) ~ (but ~>! constant) ^^ {
    case id ~ lit ~ _ => ast.DecConstant(id, lit)
  }

  private def constant = accept("constant", { case c: Token.CONSTANT => c })

  private def assignment = accept("=", { case t: Token.ASSIGNMENT => t })

  private def Id: Parser[ast.Ast.Id] = accept("identifier", { case Token.IDENTIFIER(s) => s })

  private def literal: Parser[ast.AstLiteral] =
    accept("literal", {
      case Token.INTEGER_LITERAL(value) => ast.IntegerLiteral(value)
    })

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

  def parseInputFile[T](p: Parser[T])(f: File): util.Result[T] = {
    Metadata.file = f
    Using.resource(Source.fromFile(f)) { source =>
      implicit val ctx: Context = Context()
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

  class TokenReader(tokens: List[Token]) extends ScalaParserReader[Token] {
    override def first: Token = tokens.head

    override def rest: ScalaParserReader[Token] = new TokenReader(tokens.tail)

    override def pos: Position = tokens.headOption.map(_.pos).getOrElse(NoPosition)

    override def atEnd: Boolean = tokens.isEmpty
  }

  private object Metadata {
    var file: File = _
  }
}