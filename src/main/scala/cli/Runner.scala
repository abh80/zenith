package cli

import analysis.semantics.Scope
import analysis.{Analyzer, SimpleAstVisitor}
import ast.AstNode
import generator.{CodeGenerator, Target}
import scopt.OptionParser
import syntax.{CompilationContext, Parser}
import util.Result

import java.io.File
import scala.annotation.switch

object Runner {
  private val targetLanguages = Set("javascript", "js")

  def main(args: Array[String]): Unit = {
    val cliParser = createParser()

    cliParser.parse(args, CliConfig()) match {
      case Some(config) =>
        runCompilation(config) match {
          case Left(errs) =>
            errs.foreach(_.print())
            sys.exit(1)
          case Right(output) =>
            writeOutput(config, output)
            if (config.verbose) println("Compilation successful!")
        }
      case None =>
        sys.exit(1)
    }
  }

  private def createParser(): OptionParser[CliConfig] =
    new OptionParser[CliConfig]("Zenith") {
      head("Zenith", "1.0")

      arg[File]("<input-file>")
        .required()
        .action((x, c) => c.copy(inputFile = Some(x)))
        .text("Enter the zenith source to compile from, file extension can be (.z) but not required")
        .validate(f =>
          if f.exists() && f.isFile then success
          else failure(s"Source file '${f.getPath}' cannot be opened, please check if it's a file and path is correct")
        )

      opt[File]('o', "output").valueName("<output-file>").action((x, c) => c.copy(outputFile = Some(x)))
        .text("Output file (default: stdout)")

      opt[String]('t', "target").valueName("<language>").validate(
        v => if targetLanguages.contains(v.toLowerCase)
        then success
        else failure(s"Target must be one of ${targetLanguages.mkString(",")}")
      ).action((x, c) => c.copy(targetLanguage = parseTargetLanguageFromString(x.toLowerCase))).text(s"Target language: ${targetLanguages.mkString(",")}")


      opt[Unit]('v', "verbose")
        .action((_, c) => c.copy(verbose = true))
        .text("Enable verbose output")

      opt[Unit]("debug")
        .action((_, c) => c.copy(debug = true))
        .text("Enable debug mode")

      opt[Unit]("show-ast")
        .action((_, c) => c.copy(showAst = true))
        .text("Print the Abstract Syntax Tree")

      opt[Unit]("show-analysis")
        .action((_, c) => c.copy(showAnalysis = true))
        .text("Print semantic analysis results")

      help("help")
        .text("Show this help message")

      version("version")
        .text("Show version information")

      note(
        """
Examples:
  zenith input.z                         # Compile to JavaScript (stdout)
  zenith input.z -o output.js             # Compile to JavaScript file
  zenith input.z --show-ast --verbose     # Show AST and verbose output
      """)
    }

  private def parseTargetLanguageFromString(str: String): Target =
    (str: @switch) match {
      case "javascript" | "js" =>
        Target.JavaScript
    }

  private def runCompilation(config: CliConfig): Result[String] = {
    val context = new CompilationContext()

    for {
      ast <- {
        if (config.verbose) println("Parsing input file...")
        Parser.parseInputFile(Parser.elementSequence)(config.inputFile.get)
      }

      _ <- {
        if (config.showAst) {
          println("=== Abstract Syntax Tree ===")
          ast.foreach(node => println(s"${node.id}: ${node.data}"))
          println()
        }
        Result.success(())
      }

      analyzer <- {
        if (config.verbose) println("Performing semantic analysis...")
        performSemanticAnalysis(ast, config)
      }

      _ <- {
        if (config.showAnalysis) {
          println("=== Semantic Analysis Results ===")
          println(s"Symbols: ${analyzer.symbolScopeMap.keys.mkString(", ")}")
          println(s"Types: ${analyzer.typeMap}")
          println()
        }
        Result.success(())
      }

      // Generate code
      output <- {
        if (config.verbose) println(s"Generating ${config.targetLanguage} code...")
        val generator = CodeGenerator.target(config.targetLanguage)
        generator.generateCode(ast, analyzer)
      }

    } yield output
  }

  private def performSemanticAnalysis(ast: List[AstNode[?]], config: CliConfig): Result[Analyzer] = {
    val visitor = new SimpleAstVisitor(ast)
    val initialAnalyzer = Analyzer(
      level = 0,
      currentScope = Scope.createGlobal()
    )

    val res = visitor.foldWithRecovery(Result.success(initialAnalyzer)) { (result, node) =>
      result match {
        case Left(problems) => Right(Left(problems))
        case Right(analyzer) =>
          analyzer.analyzeNode(node) match {
            case Left(problems) => Right(Left(problems))
            case Right(newAnalyzer) => Right(Right(newAnalyzer))
          }
      }
    }
    res.flatten
  }

  private def writeOutput(config: CliConfig, output: String): Unit =
    config.outputFile match {
      case Some(file) =>
        import java.nio.file.{Files, Paths, StandardOpenOption}
        Files.write(
          Paths.get(file.getPath),
          output.getBytes("UTF-8"),
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING
        )
        if (config.verbose) println(s"Output written to: ${file.getPath}")

      case None =>
        println(output)
    }
}
