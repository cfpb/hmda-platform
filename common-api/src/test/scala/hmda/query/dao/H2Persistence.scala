package hmda.query.dao

import slick.jdbc.JdbcBackend.Database

trait H2Persistence extends ProfileComponent with DatabaseComponent {
  val profile = slick.jdbc.H2Profile
  val db = Database.forConfig("testDB")
}
