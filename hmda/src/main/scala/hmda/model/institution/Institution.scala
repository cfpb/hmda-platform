package hmda.model.institution

case class Institution(
    activityYear: Int,
    LEI: Option[String],
    agency: Option[Agency],
    intitutionType: Option[InstitutionType],
    institutionId_2017: Option[String],
    taxId: Option[String],
    rssd: Option[String],
    emailDomain: Option[String],
    respondent: Respondent,
    parent: Parent,
    assets: Option[Int],
    otherLenderCode: Option[Int],
    topHolder: TopHolder,
    hmdaFiler: Boolean
)
