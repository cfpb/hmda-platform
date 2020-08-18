package hmda.institution.query

import hmda.query.DbConfiguration._
import hmda.query.repository.TableRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait InstitutionNoteHistoryComponent {

  import dbConfig.profile.api._



  class InstitutionNoteHistoryTable(tag: Tag, tableName: String) extends Table[InstitutionNoteHistoryEntity](tag, "institutions_history_notes") {
    def id          = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def year             = column[String]("year")
    def lei             = column[String]("lei")
    def historyId    = column[String]("history_id")
    def notes                     = column[String]("notes")
    def updatedPanel                     = column[String]("updated_panel")
    def * =
      (id,
       year,
        lei,
        historyId,
        notes,
        updatedPanel
      ) <> (InstitutionNoteHistoryEntity.tupled, InstitutionNoteHistoryEntity.unapply)
  }

  val institutionNoteHistoryTable = TableQuery[InstitutionNoteHistoryTable]((tag: Tag) => new InstitutionNoteHistoryTable(tag, "institutions_note_history"))

  class InstitutionNoteHistoryRepository(val config: DatabaseConfig[JdbcProfile])
    extends TableRepository[InstitutionNoteHistoryTable, String] {
    val table                               = institutionNoteHistoryTable
    def getId(table: InstitutionNoteHistoryTable) = table.lei
    def deleteById(lei: String)             = db.run(filterById(lei).delete)

    def createSchema() = db.run(table.schema.create)
    def dropSchema()   = db.run(table.schema.drop)

    def findInstitutionHistory(year: String,lei: String) = {
       val query       = table.filter(institution => institution.lei.toUpperCase === lei.toUpperCase && institution.year === year )
      db.run(query.result)
    }
  }
}
