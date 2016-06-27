package hmda.validation.engine.lar.quality

import java.io.File

import hmda.parser.fi.lar.{ LarCsvParser, LarGenerators }
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

import scala.io.Source

class LarQualityEngineSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers
    with LarGenerators
    with LarQualityEngine {

  property("A LAR must pass quality checks") {
    for (x <- 1 to 5) {
      val lines = Source.fromFile(new File("parser/src/test/resources/txt/QualityMacroPasses_Test" + x + ".txt")).getLines()
      val lars = lines.drop(1).map(l => LarCsvParser(l))

      lars.foreach { lar =>
        checkQuality(lar).isSuccess mustBe true
      }
    }
  }

  property("There should be no duplicates in qualityEditsList") {
    qualityEditsList.distinct.length mustBe qualityEditsList.length
  }

  property("There should be edits equal to the number of edit files - 1 (for Q022)") {
    val pathOrig = getClass.getResource("").getPath
    val pathSplit = pathOrig.split("/").dropRight(8).mkString("/")
    val pathFinal = pathSplit + "/src/main/scala/hmda/validation/rules/lar/quality"
    val editFiles = Option((new File(pathFinal).list).length).getOrElse(0)
    editFiles - 1 mustBe qualityEditsList.length
  }

}
