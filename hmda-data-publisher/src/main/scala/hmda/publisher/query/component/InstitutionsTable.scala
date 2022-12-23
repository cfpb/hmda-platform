package hmda.publisher.query.component
import hmda.publisher.query.panel.InstitutionEntity
import hmda.query.DbConfiguration.dbConfig.profile.api._

class InstitutionsTable(tag: Tag, tableName: String) extends Table[InstitutionEntity](tag, tableName) {

  def lei = column[String]("lei", O.PrimaryKey)
  def activityYear = column[Int]("activity_year")
  def agency = column[Int]("agency")
  def institutionType = column[Int]("institution_type")
  def id2017 = column[String]("id2017")
  def taxId = column[String]("tax_id")
  def rssd = column[Int]("rssd")
  def respondentName = column[String]("respondent_name")
  def respondentState = column[String]("respondent_state")
  def respondentCity = column[String]("respondent_city")
  def parentIdRssd = column[Int]("parent_id_rssd")
  def parentName = column[String]("parent_name")
  def assets = column[Long]("assets")
  def otherLenderCode = column[Int]("other_lender_code")
  def topHolderIdRssd = column[Int]("topholder_id_rssd")
  def topHolderName = column[String]("topholder_name")
  def hmdaFiler = column[Boolean]("hmda_filer")
  override def * =
    (
      lei,
      activityYear,
      agency,
      institutionType,
      id2017,
      taxId,
      rssd,
      respondentName,
      respondentState,
      respondentCity,
      parentIdRssd,
      parentName,
      assets,
      otherLenderCode,
      topHolderIdRssd,
      topHolderName,
      hmdaFiler
    ) <> (InstitutionEntity.tupled, InstitutionEntity.unapply)
}