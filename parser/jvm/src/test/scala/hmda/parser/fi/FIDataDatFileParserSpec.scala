package hmda.parser.fi

import hmda.model.ResourceUtils
import org.scalatest.{ AsyncFlatSpec, MustMatchers }

import scala.concurrent.ExecutionContext

class FIDataDatFileParserSpec extends AsyncFlatSpec with MustMatchers with ResourceUtils {
  override def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  "A DAT File" must "be parsed in the JVM" in {
    val lines = resourceLines("/dat/sample.dat")
    val parser = new FIDataDatParser
    val data = parser.read(lines.toIterable)
    val ts = data.ts
    val lars = data.lars
    ts.respondent.id mustBe "0123456789"
    lars.size mustBe 3
  }

}
