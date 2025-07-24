package cli

import generator.Target

import java.io.File

case class CliConfig(
                      inputFile: Option[File] = None,
                      outputFile: Option[File] = None,
                      targetLanguage: Target = Target.JavaScript,
                      verbose: Boolean = false,
                      debug: Boolean = false,
                      showAst: Boolean = false,
                      showAnalysis: Boolean = false
                    )
