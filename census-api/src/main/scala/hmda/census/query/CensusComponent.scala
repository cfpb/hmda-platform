package hmda.census.query

import hmda.census.api.http.CensusConverter
import hmda.model.census.Census
import hmda.model.institution.Institution
import hmda.query.DbConfiguration._
import hmda.query.repository.TableRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait CensusComponent {

  import dbConfig.profile.api._

  class CensusTable(tag: Tag) extends Table[CensusEntity](tag, "census2018") {

    def collectionYear = column[Int]("collection_yr")
    def msaMd = column[Int]("msa_md")
    def state = column[String]("state")
    def county = column[String]("county")
    def tract = column[Int]("tract")
    def medianIncome = column[Int]("median_income")
    def population = column[Int]("population")
    def minorityPopulationPercent = column[Double]("minority_population_per")
    def occupiedUnits = column[Int]("owner_occupied_units")
    def oneToFourFamilyUnits = column[Int]("one_to_four_family_units")
    def tractMfi = column[Int]("tract_mfi")
    def tractToMsaIncomePercent = column[Double]("tract_to_msa_income_per")
    def medianAge = column[Int]("med_age")

    def * =
      (collectionYear,
       msaMd,
       state,
       county,
       tract,
       medianIncome,
       population,
       minorityPopulationPercent,
       occupiedUnits,
       oneToFourFamilyUnits,
       tractMfi,
       tractToMsaIncomePercent,
       medianAge) <> (CensusEntity.tupled, CensusEntity.unapply)
  }

  val censusTable = TableQuery[CensusTable]

  class CensusRepository(val config: DatabaseConfig[JdbcProfile])
      extends TableRepository[CensusTable, Int] {
    val table = censusTable
    def getId(table: CensusTable) = table.collectionYear
    def deleteById(collectionYear: Int) =
      db.run(filterById(collectionYear).delete)

    def createSchema() = db.run(table.schema.create)
    def dropSchema() = db.run(table.schema.drop)
  }

  def findByCollectionYr(collectionYr: Int)(
      implicit ec: ExecutionContext,
      censusRepository: CensusRepository): Future[Seq[Census]] = {
    val db = censusRepository.db
    val table = censusRepository.table
    for {
      census <- db.run(table.filter(_.collectionYear === collectionYr).result)
    } yield {
      census.map { c =>
        CensusConverter.convert(c)
      }
    }
  }
}
