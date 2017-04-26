package hmda.validation.engine.ts.quality

import java.io.File

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.parser.fi.ts.TsCsvParser
import hmda.validation.context.ValidationContext
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

import scala.concurrent.ExecutionContext
import scala.io.Source

class TsQualityEngineSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers
    with TsQualityEngine {

  implicit val system = ActorSystem("ts-quality-test")
  implicit val materializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  private val ctx = ValidationContext(None, None)

  property("A Transmittal Sheet must pass quality checks") {
    val line = Source.fromFile(new File("parser/jvm/src/test/resources/txt/clean_10-lars.txt")).getLines().take(1)
    val ts = line.map(l => TsCsvParser(l))

    ts.foreach { ts =>
      checkQuality(ts.right.get, ctx).map(validation => validation.isSuccess mustBe true)
    }
  }
}
