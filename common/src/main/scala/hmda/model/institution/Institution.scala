package hmda.model.institution

object Institution {
  def empty: Institution = Institution(
    2018,
    "",
    UndeterminedAgency,
    UndeterminedInstitutionType,
    None,
    None,
    -1,
    Nil,
    Respondent.empty,
    Parent.empty,
    -1,
    -1,
    TopHolder.empty,
    false
  )
}

case class Institution(
    activityYear: Int,
    LEI: String,
    agency: Agency,
    institutionType: InstitutionType,
    institutionId_2017: Option[String],
    taxId: Option[String],
    rssd: Int,
    emailDomains: Seq[String],
    respondent: Respondent,
    parent: Parent,
    assets: Int,
    otherLenderCode: Int,
    topHolder: TopHolder,
    hmdaFiler: Boolean
) {
  def toCSV: String = {
    s"$activityYear|$LEI|${agency.code}|${institutionType.code}|" +
      s"${institutionId_2017.getOrElse("")}|${taxId.getOrElse("")}|$rssd|${emailDomains
        .mkString(",")}|" +
      s"${respondent.name.getOrElse("")}|${respondent.state.getOrElse("")}|${respondent.city
        .getOrElse("")}|" +
      s"${parent.idRssd}|${parent.name.getOrElse("")}|$assets|${otherLenderCode}|" +
      s"${topHolder.idRssd}|${topHolder.name.getOrElse("")}|$hmdaFiler"
  }
}
