package hmda.api.model.admin

import hmda.model.apor.{ APOR, RateType }

case class ModifyAporRequest(rateType: RateType, newApor: APOR)
