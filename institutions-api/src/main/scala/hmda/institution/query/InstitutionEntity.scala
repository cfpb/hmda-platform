package hmda.institution.query

case class InstitutionEntity(
  lei: String = "",
  activityYear: Int = 0,
  agency: Int = 9,
  institutionType: String = "",
  id2017: String = "",
  taxId: String = "",
  rssd: String = "",
  respondentName: String = "",
  respondentState: String = "",
  respondentCity: String = "",
  parentIdRssd: String = "",
  parentName: String = "",
  assets: String = "",
  otherLenderCode: String = "",
  topHolderIdRssd: String = "",
  topHolderName: String = "",
  hmdaFiler: Boolean = false,
  quarterlyFiler: Boolean = false,
  quarterlyFilerHasFiledQ1: Boolean = false,
  quarterlyFilerHasFiledQ2: Boolean = false,
  quarterlyFilerHasFiledQ3: Boolean = false,
  notes: String = ""
) {
  def isEmpty: Boolean = lei == ""
}
