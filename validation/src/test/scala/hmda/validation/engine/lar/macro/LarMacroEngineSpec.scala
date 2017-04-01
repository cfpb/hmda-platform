package hmda.validation.engine.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.validation.context.ValidationContext
import hmda.validation.rules.lar.`macro`.MacroTestData
import org.scalatest.{ AsyncWordSpec, MustMatchers }

import scala.language.postfixOps
import scalaz.{ Failure, Success }

class LarMacroEngineSpec extends AsyncWordSpec with MustMatchers with LarMacroEngine {

  implicit val system = ActorSystem("macro-edits-test")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val lars = MacroTestData.lars
  val larSource = Source.fromIterator(() => lars.toIterator)
  val ctx = ValidationContext(None, None)

  "Lar Macro Engine" must {
    "pass macro edits" in {
      checkMacro(larSource, ctx).map(validation => validation mustBe a[Success[_]])
    }
    "fail Q007" in {
      val larsQ007 = lars.map(lar => lar.copy(actionTakenType = 2))
      val q007Source = Source.fromIterator(() => larsQ007.toIterator)
      checkMacro(q007Source, ctx).map(validation => validation mustBe a[Failure[_]])
    }
    "fail Q008" in {
      val larsQ008 = lars.map(lar => lar.copy(actionTakenType = 4))
      val q008Source = Source.fromIterator(() => larsQ008.toIterator)
      checkMacro(q008Source, ctx).map(validation => validation mustBe a[Failure[_]])
    }
  }

}
