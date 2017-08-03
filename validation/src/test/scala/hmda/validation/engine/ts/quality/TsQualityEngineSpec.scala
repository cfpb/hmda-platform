package hmda.validation.engine.ts.quality

import java.io.File

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.model.fi.ts.TransmittalSheet
import hmda.parser.fi.ts.TsCsvParser
import hmda.validation.context.ValidationContext
import org.scalatest.{ AsyncWordSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks

import scala.concurrent.ExecutionContext
import scala.io.Source

class TsQualityEngineSpec
    extends AsyncWordSpec
    with PropertyChecks
    with MustMatchers
    with TsQualityEngine {

  implicit val system = ActorSystem("ts-quality-test")
  implicit val materializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  private val ctx = ValidationContext(None, None)

  "A Transmittal Sheet" must {
    "pass quality checks" in {
      val line = Source.fromFile(new File("parser/jvm/src/test/resources/txt/clean_10-lars.txt")).getLines().take(1)
      val ts = line.map(l => TsCsvParser(l).right.getOrElse(TransmittalSheet())).toList.head

      checkQuality(ts, ctx).isSuccess mustBe true
    }
  }
}
