package integration

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import testutil.TestHelper
import java.io.File
import scala.sys.process._

class StringSpec extends AnyFunSuite with Matchers {

  def compileAndRun(zenithCode: String): util.Result[String] = {
    for {
      jsCode <- TestHelper.compileString(zenithCode)
    } yield {
      val tempFile = File.createTempFile("zenith_string_test_", ".js")
      tempFile.deleteOnExit()
      val writer = new java.io.PrintWriter(tempFile)
      try {
        writer.write(jsCode)
      } finally {
        writer.close()
      }
      
      try {
        s"node ${tempFile.getAbsolutePath}".!!.trim
      } catch {
        case e: Exception => s"Error: ${e.getMessage}"
      }
    }
  }

  test("String literal print") {
    val code = """
      print "Hello World"
    """
    val result = compileAndRun(code)
    result.isRight shouldBe true
    result.toOption.get should include("Hello World")
  }

  test("String concatenation using 'plus'") {
    val code = """
      str1 = "Hello"
      str2 = "World"
      res = str1 plus " " plus str2
      print res
    """
    val result = compileAndRun(code)
    result.isRight shouldBe true
    result.toOption.get should include("Hello World")
  }
  
  test("String concatenation using 'added to'") {
    val code = """
      part1 = "Physics"
      part2 = "Engine"
      res = part1 added to " " added to part2
      print res
    """
    val result = compileAndRun(code)
    result.isRight shouldBe true
    result.toOption.get should include("Physics Engine")
  }
  
  test("Mixed concatenation (String + Integer)") {
    val code = """
      val = 42
      res = "Answer: " plus val
      print res
    """
    val result = compileAndRun(code)
    result.isRight shouldBe true
    result.toOption.get should include("Answer: 42")
  }

  test("Mixed concatenation (Integer + String)") {
    val code = """
      n = 10
      res = n plus " apples"
      print res
    """
    val result = compileAndRun(code)
    result.isRight shouldBe true
    result.toOption.get should include("10 apples")
  }
}
