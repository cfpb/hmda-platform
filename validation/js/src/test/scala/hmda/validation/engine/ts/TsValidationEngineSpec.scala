package hmda.validation.engine.ts

import hmda.parser.fi.ts.TsGenerators
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

import scala.concurrent.ExecutionContext

//TODO: fails if JSExecutionContext.Implicits.queue is used (runNow is deprecated, but works)

class TsValidationEngineSpec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators with CommonTsValidationEngine with ScalaFutures with CommonTsValidationSpec {

  override implicit val ec: ExecutionContext = scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

}
