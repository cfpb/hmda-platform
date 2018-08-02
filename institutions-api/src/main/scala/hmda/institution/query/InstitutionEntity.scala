package hmda.institution.query

case class InstitutionEntity(
    lei: String = "",
    activityYear: Int = 0,
    agency: Int = 0,
    institutionType: Int = 0,
    id2017: String = "",
    taxId: String = "",
    rssd: String = "",
    emailDomains: String = "",
    respondentName: String = "",
    respondentState: String = "",
    respondentCity: String = "",
    parentIdRssd: Int = 0,
    parentName: String = "",
    assets: Int = 0,
    otherLenderCode: Int = 0,
    topHolderIdRssd: Int = 0,
    topHolderName: String = "",
    hmdaFiler: Boolean = false
)
