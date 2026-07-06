package hmda.query.repository

import hmda.query.institution.InstitutionEntity
import hmda.query.DbConfiguration._
import hmda.query.repository.TableRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait InstitutionComponent {

  import dbConfig.profile.api._

  class InstitutionsTable(tag: Tag, tableName: String) extends Table[InstitutionEntity](tag, tableName) {
    def lei             = column[String]("lei", O.PrimaryKey)
    def activityYear    = column[Int]("activity_year")
    def agency          = column[Int]("agency")
    def institutionType = column[Int]("institution_type")
    def id2017          = column[String]("id2017")
    def taxId           = column[String]("tax_id")
    def rssd            = column[Int]("rssd")
    def respondentName  = column[String]("respondent_name")
    def respondentState = column[String]("respondent_state")
    def respondentCity  = column[String]("respondent_city")
    def parentIdRssd    = column[Int]("parent_id_rssd")
    def parentName      = column[String]("parent_name")
    def assets          = column[Long]("assets")
    def otherLenderCode = column[Int]("other_lender_code")
    def topHolderIdRssd = column[Int]("topholder_id_rssd")
    def topHolderName   = column[String]("topholder_name")
    def hmdaFiler       = column[Boolean]("hmda_filer")

    def * =
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

  class InstitutionRepository(val databaseConfig: DatabaseConfig[JdbcProfile]) {
    import databaseConfig.profile.api._
    private val db = databaseConfig.db

    //dynamic institution tables
    val institutionsTable2018 = TableQuery[InstitutionsTable]((tag: Tag) => new InstitutionsTable(tag, "institutions2018"))
    val table2018         = institutionsTable2018

    val institutionsTable2019 = TableQuery[InstitutionsTable]((tag: Tag) => new InstitutionsTable(tag, "institutions2019"))
    val table2019         = institutionsTable2019

    val institutionsTable2020 = TableQuery[InstitutionsTable]((tag: Tag) => new InstitutionsTable(tag, "institutions2020"))
    val table2020         = institutionsTable2020

    val institutionsTable2021 = TableQuery[InstitutionsTable]((tag: Tag) => new InstitutionsTable(tag, "institutions2021"))
    val table2021         = institutionsTable2021

    val institutionsTable2022 = TableQuery[InstitutionsTable]((tag: Tag) => new InstitutionsTable(tag, "institutions2022"))
    val table2022         = institutionsTable2022

    val institutionsTable2023 = TableQuery[InstitutionsTable]((tag: Tag) => new InstitutionsTable(tag, "institutions2023"))
    val table2023         = institutionsTable2023
    
    val institutionsTable2024 = TableQuery[InstitutionsTable]((tag: Tag) => new InstitutionsTable(tag, "institutions2024"))
    val table2024         = institutionsTable2024

    val institutionsTable2025 = TableQuery[InstitutionsTable]((tag: Tag) => new InstitutionsTable(tag, "institutions2025"))
    val table2025         = institutionsTable2025

    val institutionsTable2026 = TableQuery[InstitutionsTable]((tag: Tag) => new InstitutionsTable(tag, "institutions2026"))
    val table2026         = institutionsTable2026

    //snapshot tables    
    val snapshotInstitutionsTable2024 = TableQuery[InstitutionsTable]((tag: Tag) => new InstitutionsTable(tag, "institutions2024_snapshot_v3"))
    val snapshotTable2024         = snapshotInstitutionsTable2024

    val snapshotInstitutionsTable2025 = TableQuery[InstitutionsTable]((tag: Tag) => new InstitutionsTable(tag, "institutions2025_snapshot_06012026_v2"))
    val snapshotTable2025         = snapshotInstitutionsTable2025

    
    def fetchYearTable(year: Int, snapshot: Boolean) = 
    snapshot match {
        case true =>
            year match {
                case 2018 => table2018
                case 2019 => table2019
                case 2020 => table2020
                case 2021 => table2021
                case 2022 => table2022
                case 2023 => table2023
                case 2024 => snapshotTable2024
                case 2025 => snapshotTable2025
                case _    => table2025
            }
        case false =>
            year match {
                case 2018 => table2018
                case 2019 => table2019
                case 2020 => table2020
                case 2021 => table2021
                case 2022 => table2022
                case 2023 => table2023
                case 2024 => table2024
                case 2025 => table2025
                case 2026 => table2026
                case _    => table2025
            }
    }

    def findByLei(lei: String, year: Int, snapshot: Boolean) =
      db.run(fetchYearTable(year, snapshot).filter(_.lei === lei).result)

    def getAllFilers(year: Int, snapshot: Boolean) =
      db.run(fetchYearTable(year, snapshot).filter(_.hmdaFiler === true).result)

    def getFilteredFilers(bankFilterList: Array[String], year: Int, snapshot: Boolean) =
      db.run(
        fetchYearTable(year, snapshot)
          .filter(_.hmdaFiler === true)
          .filterNot(_.lei.toUpperCase inSet bankFilterList)
          .result
      )
  }
}
