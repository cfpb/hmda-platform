package hmda.validation.rules.ts.quality

import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.validation.dsl.{ Result, Success }
import hmda.validation.rules.EditCheck

class Q033(institution: Institution) extends EditCheck[TransmittalSheet] {
  override def name: String = "Q033"

  override def apply(input: TransmittalSheet): Result = {
    Success() // TODO replace with real implementation
  }
}
