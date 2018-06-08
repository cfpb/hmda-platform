package hmda.api.model.public

import hmda.model.institution.HmdaFiler

case class HmdaFilerResponse(institutions: Set[HmdaFiler])
case class MsaMdResponse(institution: HmdaFiler, msaMds: Seq[MsaMd])
case class MsaMd(id: String, name: String)
