package integration

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import testutil.TestHelper
import java.io.File
import scala.sys.process._

class FloatSpec extends AnyFunSuite with Matchers {

  def compileAndRun(zenithCode: String): util.Result[String] = {
    for {
      jsCode <- TestHelper.compileString(zenithCode)
    } yield {
      val tempFile = File.createTempFile("zenith_float_test_", ".js")
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

  test("Float literal assignment and print") {
    val code = """
      val = 3.14
      print "Value is $val"
    """
    val result = compileAndRun(code)
    result.isRight shouldBe true
    result.toOption.get should include("Value is 3.14")
  }

  test("Float arithmetic") {
    val code = """
      a = 2.5
      b = 1.5
      sum = a plus b
      print "Sum is $sum"
    """
    val result = compileAndRun(code)
    result.isRight shouldBe true
    // JS prints integers for whole numbers
    result.toOption.get should include("Sum is 4")
  }

  test("Float and Integer mixed arithmetic") {
    val code = """
      a = 2.5
      b = 2
      prod = a times b
      print "Prod is $prod"
    """
    val result = compileAndRun(code)
    result.isRight shouldBe true
    result.toOption.get should include("Prod is 5")
  }

  test("Explicit Float Type Definition") {
    val code = """
      pi is float = 3.14159 but constant
      print "Pi is $pi"
    """
     val result = compileAndRun(code)
    result.isRight shouldBe true
    result.toOption.get should include("Pi is 3.14159")
  }

  test("Floats with decimal keyword") {
     val code = """
       a is decimal = 10.5
       print "Val is $a"
     """
     val result = compileAndRun(code)
     result.isRight shouldBe true
     result.toOption.get should include("Val is 10.5")
  }
}
