package hmda.institution.query

case class InstitutionEntity(
  lei: String = "",
  activityYear: Int = 0,
  agency: Int = 9,
  institutionType: Int = -1,
  id2017: String = "",
  taxId: String = "",
  rssd: Int = -1,
  respondentName: String = "",
  respondentState: String = "",
  respondentCity: String = "",
  parentIdRssd: Int = -1,
  parentName: String = "",
  assets: Long = 0,
  otherLenderCode: Int = -1,
  topHolderIdRssd: Int = -1,
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
