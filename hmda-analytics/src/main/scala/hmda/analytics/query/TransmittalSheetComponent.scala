package hmda.analytics.query

import hmda.query.DbConfiguration._
import hmda.query.repository.TableRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import scala.concurrent.Future

trait TransmittalSheetComponent {

  import dbConfig.profile.api._

  class TransmittalSheetTable(tag: Tag)
      extends Table[TransmittalSheetEntity](tag, "transmittalsheet2018") {

    def lei = column[String]("lei", O.PrimaryKey)
    def id = column[Int]("id")
    def institutionName = column[String]("institution_name")
    def year = column[Int]("year")
    def quarter = column[Int]("quarter")
    def name = column[String]("name")
    def phone = column[String]("phone")
    def email = column[String]("email")
    def street = column[String]("street")
    def city = column[String]("city")
    def state = column[String]("state")
    def zipCode = column[String]("zip_code")
    def agency = column[Int]("agency")
    def totalLines = column[Int]("total_lines")
    def taxId = column[String]("tax_id")

    override def * =
      (
        lei,
        id,
        institutionName,
        year,
        quarter,
        name,
        phone,
        email,
        street,
        city,
        state,
        zipCode,
        agency,
        totalLines,
        taxId
      ) <> (TransmittalSheetEntity.tupled, TransmittalSheetEntity.unapply)
  }

  val transmittalSheetTable = TableQuery[TransmittalSheetTable]

  class TransmittalSheetRepository(val config: DatabaseConfig[JdbcProfile])
      extends TableRepository[TransmittalSheetTable, String] {

    override val table: config.profile.api.TableQuery[TransmittalSheetTable] =
      transmittalSheetTable

    override def getId(row: TransmittalSheetTable): config.profile.api.Rep[Id] =
      row.lei

    def createSchema() = db.run(table.schema.create)
    def dropSchema() = db.run(table.schema.drop)

    def insert(ts: TransmittalSheetEntity): Future[Int] = {
      db.run(table += ts)
    }

    def findByLei(lei: String): Future[Seq[TransmittalSheetEntity]] = {
      db.run(table.filter(_.lei.toUpperCase === lei.toUpperCase).result)
    }

    def deleteByLei(lei: String): Future[Int] = {
      db.run(table.filter(_.lei.toUpperCase === lei.toUpperCase).delete)
    }

    def updateByLei(ts: TransmittalSheetEntity): Future[Int] = {
      db.run(table.insertOrUpdate(ts))
    }

    def count(): Future[Int] = {
      db.run(table.size.result)
    }
  }

}
