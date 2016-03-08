package hmda.parser.fi

import java.io.File

import org.scalatest.{ AsyncFlatSpec, MustMatchers }

import scala.concurrent.ExecutionContext

class FIDataDatFileParserSpec extends AsyncFlatSpec with MustMatchers {
  override def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  "A DAT File" must "be parsed in the JVM" in {
    val file = new File("parser/shared/src/test/resources/dat/sample.dat")
    val datFile = io.Source.fromFile(file)
    val lines = datFile.getLines().toIterable
    val parser = new FIDataDatParser
    val data = parser.read(lines)
    val ts = data.ts
    val lars = data.lars
    ts.respondent.id mustBe "0123456789"
    lars.size mustBe 3
  }

}
