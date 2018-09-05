package hmda.institution.query

case class InstitutionEntity(
    lei: String = "",
    activityYear: Int = 0,
    agency: Int = -1,
    institutionType: Int = -1,
    id2017: String = "",
    taxId: String = "",
    rssd: Int = -1,
    respondentName: String = "",
    respondentState: String = "",
    respondentCity: String = "",
    parentIdRssd: Int = -1,
    parentName: String = "",
    assets: Int = 0,
    otherLenderCode: Int = -1,
    topHolderIdRssd: Int = -1,
    topHolderName: String = "",
    hmdaFiler: Boolean = false
) {
  def isEmpty: Boolean = lei == ""
}
