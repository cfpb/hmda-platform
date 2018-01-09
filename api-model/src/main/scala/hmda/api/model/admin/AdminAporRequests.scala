package hmda.api.model.admin

import hmda.model.apor.{ APOR, RateType }

object AdminAporRequests {
  case class CreateAporRequest(newApor: APOR, rateType: RateType)
  case class ModifyAporRequest(newApor: APOR, rateType: RateType)
}

