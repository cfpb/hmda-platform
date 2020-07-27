package hmda.institution.query

import hmda.query.DbConfiguration._
import hmda.query.repository.TableRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

trait InstitutionNoteHistoryComponent {

  import dbConfig.profile.api._



  class InstitutionNoteHistoryTable(tag: Tag, tableName: String) extends Table[InstitutionNoteHistoryEntity](tag, tableName) {
    def id          = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def lei             = column[String]("lei")
    def historyId    = column[String]("history_id")
    def notes                     = column[String]("notes")
    def * =
      (id,
        lei,
        historyId,
        notes
      ) <> (InstitutionNoteHistoryEntity.tupled, InstitutionNoteHistoryEntity.unapply)
  }

  val institutionNoteHistoryTable = TableQuery[InstitutionNoteHistoryTable]((tag: Tag) => new InstitutionNoteHistoryTable(tag, "institutions_note_history"))

  class InstitutionNoteHistoryRepository(val config: DatabaseConfig[JdbcProfile], tableName: String)
    extends TableRepository[InstitutionNoteHistoryTable, String] {
    val institutionNoteHistoryTable               = TableQuery[InstitutionNoteHistoryTable]((tag: Tag) => new InstitutionNoteHistoryTable(tag, tableName))
    val table                               = institutionNoteHistoryTable
    def getId(table: InstitutionNoteHistoryTable) = table.lei
    def deleteById(lei: String)             = db.run(filterById(lei).delete)

    def createSchema() = db.run(table.schema.create)
    def dropSchema()   = db.run(table.schema.drop)
  }
}
