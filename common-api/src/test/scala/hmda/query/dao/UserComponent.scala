package hmda.query.dao

trait UserComponent { this: DatabaseComponent with ProfileComponent =>

  import slick.lifted.Tag
  import profile.api._

  class UserTable(tag: Tag) extends Table[User](tag, "user") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")

    def * = (id.?, name) <> (User.tupled, User.unapply)
  }

  object UserRepository extends Repository[UserTable, Long](profile, db) {
    import this.profile.api._

    val table = TableQuery[UserTable]
    def getId(table: UserTable) = table.id
  }

}
