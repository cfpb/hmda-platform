package hmda.api.model.public

import hmda.model.institution.HmdaFiler

case class HmdaFilerResponse(institutions: Set[HmdaFiler])
