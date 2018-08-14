package hmda.model.institution

object Institution {
  def empty: Institution = Institution(
    2018,
    None,
    Some(UndeterminedAgency),
    Some(UndeterminedInstitutionType),
    None,
    None,
    None,
    Nil,
    Respondent.empty,
    Parent.empty,
    None,
    None,
    TopHolder.empty,
    false
  )
}

case class Institution(
    activityYear: Int,
    LEI: Option[String],
    agency: Option[Agency],
    institutionType: Option[InstitutionType],
    institutionId_2017: Option[String],
    taxId: Option[String],
    rssd: Option[String],
    emailDomains: Seq[String],
    respondent: Respondent,
    parent: Parent,
    assets: Option[Int],
    otherLenderCode: Option[Int],
    topHolder: TopHolder,
    hmdaFiler: Boolean
)
