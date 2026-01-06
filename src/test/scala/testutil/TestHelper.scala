package testutil

import syntax.{CompilationContext, Lexer, Parser}
import analysis.Analyzer
import generator.JavaScriptGenerator
import java.io.File
import java.nio.file.{Files, StandardOpenOption}

object TestHelper {
  
  given ctx: CompilationContext = CompilationContext()

  def parseString(input: String): util.Result[List[ast.AstNode[_]]] = {
    // Create a temporary file
    val tempFile = Files.createTempFile("zenith_test_", ".zenith").toFile
    tempFile.deleteOnExit()
    
    // Write the input to the file
    Files.writeString(tempFile.toPath, input, StandardOpenOption.WRITE)
    
    // Parse the file
    Parser.parseInputFile(Parser.elementSequence)(tempFile)
  }

  def compileString(input: String): util.Result[String] = {
    for {
      ast <- parseString(input)
      analyzer <- Analyzer.analyze(ast)
      code <- new JavaScriptGenerator().generateCode(analyzer)
    } yield code
  }
}
