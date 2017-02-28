package hmda.validation.rules.lar.`macro`

import com.typesafe.config.ConfigFactory
import hmda.validation.context.ValidationContext
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class Q011Spec extends PropSpec with PropertyChecks with MustMatchers {

  val config = ConfigFactory.load()
  val previousFixed = config.getInt("hmda.validation.macro.Q011.loan.previous.fixed")
  val multiplier = config.getInt("hmda.validation.macro.Q011.loan.actual.multiplier")

  property("be named Q011") {
    val ctx = ValidationContext(None, Some(2017))
    Q011.inContext(ctx).name mustBe "Q011"
  }

}
