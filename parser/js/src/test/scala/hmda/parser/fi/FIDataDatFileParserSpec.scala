package hmda.parser.fi

import hmda.js.io.FileIO
import org.scalatest.{ MustMatchers, AsyncFlatSpec }
import scala.concurrent.ExecutionContext

class FIDataDatFileParserSpec extends AsyncFlatSpec with MustMatchers with FileIO {
  override def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  implicit val ec = executionContext

  "A DAT File" must "be parsed in JS" in {
    val path = "parser/shared/src/test/resources/sample.dat"
    val f = readFile(path)
    f.map { s =>
      val parser = new FIDataDatParser
      val data = parser.read(s.split("\n"))
      val ts = data.ts
      val lars = data.lars
      ts.respondent.id mustBe "0123456789"
      lars.size mustBe 3
    }

  }

}
