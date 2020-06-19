package hmda.publisher.query.panel

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
    hmdaFiler: Boolean = false
) {
  def isEmpty: Boolean = lei == ""

  def toPSV: String = {
    s"$lei|$activityYear|$agency|" +
      s"$institutionType|$id2017|$taxId|" +
      s"$rssd|" + s"|$respondentName|$respondentState|" +
      s"$respondentCity|$parentIdRssd|$parentName|" +
      s"$assets|$otherLenderCode|$topHolderIdRssd|$topHolderName|$hmdaFiler"

  }
}
