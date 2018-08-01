package hmda.query.institution

import hmda.query.dao.{DatabaseComponent, ProfileComponent, Repository}

trait InstitutionComponent { this: DatabaseComponent with ProfileComponent =>

  import slick.lifted.Tag
  import profile.api._

  class InstitutionTable(tag: Tag)
      extends Table[InstitutionEntity](tag, "institutions2018") {
    def lei = column[String]("lei", O.PrimaryKey)
    def activityYear = column[Int]("activity_year")
    def agency = column[Int]("agency")
    def institutionType = column[Int]("institution_type")
    def id2017 = column[String]("id2017")
    def taxId = column[String]("tax_id")
    def rssd = column[String]("rssd")
    def emailDomains = column[String]("email_domains")
    def respondentName = column[String]("respondent_name")
    def respondentState = column[String]("respondent_state")
    def respondentCity = column[String]("respondent_city")
    def parentIdRssd = column[Int]("parent_id_rssd")
    def parentName = column[String]("parent_name")
    def topHolderIdRssd = column[Int]("topholder_id_rssd")
    def topHolderName = column[String]("topholder_name")

    def * =
      (lei.?,
       activityYear,
       agency,
       institutionType,
       id2017,
       taxId,
       rssd,
       emailDomains,
       respondentName,
       respondentState,
       respondentCity,
       parentIdRssd,
       parentName,
       topHolderIdRssd,
       topHolderName) <> (InstitutionEntity.tupled, InstitutionEntity.unapply)
  }

  object InstitutionRepository
      extends Repository[InstitutionTable, String](profile, db) {
    import this.profile.api._

    val table = TableQuery[InstitutionTable]
    def getId(table: InstitutionTable) = table.lei
  }

}
