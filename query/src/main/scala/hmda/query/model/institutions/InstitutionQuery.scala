package hmda.query.model.institutions

case class InstitutionQuery(
    id: String,
    agency: Int,
    filingPeriod: Int,
    activityYear: Int,
    respondentId: String,
    institutionType: String,
    cra: Boolean,
    emailDomain1: String,
    emailDomain2: String,
    emailDomain3: String,
    respondentName: String,
    respondentState: String,
    respondentCity: String,
    respondentFipsStateNumber: String,
    hmdaFilerFlag: Boolean,
    parentRespondentId: String,
    parentIdRssd: Int,
    parentName: String,
    parentCity: String,
    parentState: String,
    assets: Int,
    otherLenderCode: Int,
    topHolderIdRssd: Int,
    topHolderName: String,
    topHolderCity: String,
    topHolderState: String,
    topHolderCountry: String
) {
  def toCSV: String = {
    s"$id|$agency|$filingPeriod|$activityYear|$respondentId|$institutionType|" +
      s"$cra|$emailDomain1|$emailDomain2|$emailDomain3|" +
      s"$respondentName|$respondentState|$respondentCity|$respondentFipsStateNumber|" +
      s"$hmdaFilerFlag|$parentRespondentId|$parentIdRssd|$parentName|$parentCity|$parentState|" +
      s"$assets|$otherLenderCode|$topHolderIdRssd|$topHolderName|$topHolderCity|$topHolderState|$topHolderCountry"
  }
}
