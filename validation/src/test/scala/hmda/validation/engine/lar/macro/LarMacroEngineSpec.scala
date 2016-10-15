package hmda.validation.engine.lar.`macro`

import akka.stream.scaladsl.Source
import hmda.validation.rules.lar.`macro`.MacroTestData
import org.scalatest.{ AsyncWordSpec, MustMatchers }
import scala.language.postfixOps
import scalaz.Success

class LarMacroEngineSpec extends AsyncWordSpec with MustMatchers with LarMacroEngine {

  val lars = MacroTestData.lars
  val larSource = Source.fromIterator(() => lars.toIterator)

  "Lar Macro Engine" must {
    "pass macro edits" in {
      checkMacro(larSource).map(validation => validation mustBe a[Success[_]])
    }
  }

}
