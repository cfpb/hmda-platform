package hmda.model.institution

case class HmdaFiler(lei: String, name: String, period: String)
case class MsaMd(id: Int, name: String)
case class HmdaFilerResponse(institutions: Set[HmdaFiler])
case class MsaMdResponse(institution: HmdaFiler, msaMds: Set[MsaMd])
