package hmda.model.institution

import hmda.model.institution.HmdaFiler

case class HmdaFilerResponse(institutions: Set[HmdaFiler])
case class MsaMdResponse(institution: HmdaFiler, msaMds: Set[MsaMd])
