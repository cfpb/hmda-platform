package hmda.validation.engine

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.context.ValidationContext
import hmda.validation.rules.lar.quality._
import hmda.validation.rules.lar.syntactical.S300
import hmda.validation.rules.lar.validity._

object LarEngine extends ValidationEngine[LoanApplicationRegister] {

  override def syntacticalChecks(ctx: ValidationContext) = Vector(
    S300
  )

  override val validityChecks = Vector(
    V600,
    V610_1,
    V610_2,
    V611,
    V612_1,
    V612_2,
    V613_1,
    V613_2,
    V613_3,
    V613_4,
    V614_1,
    V614_2,
    V614_3,
    V614_4,
    V615_1,
    V615_2,
    V615_3,
    V616,
    V617,
    V618,
    V620,
    V621,
    V623,
    V628_1,
    V628_2,
    V628_3,
    V628_4,
    V630,
    V633,
    V634,
    V635_1,
    V635_2,
    V635_3,
    V635_4,
    V636_1,
    V636_2,
    V636_3,
    V637,
    V638_1,
    V638_2,
    V638_3,
    V638_4,
    V639_1,
    V639_2,
    V639_3,
    V640,
    V641,
    V642_1,
    V642_2,
    V643,
    V644_1,
    V644_2,
    V645,
    V646_1,
    V646_2,
    V647,
    V648_1,
    V648_2,
    V649,
    V651_1,
    V651_2,
    V652_1,
    V652_2,
    V655_1,
    V655_2,
    V659,
    V691,
    V695,
    V699,
    V700_1,
    V700_2,
    V701,
    V702_1,
    V702_2,
    V703_1,
    V703_2
  )

  override val qualityChecks = Vector(
    Q601,
    Q602,
    Q606,
    Q608,
    Q610,
    Q618
  )

}
