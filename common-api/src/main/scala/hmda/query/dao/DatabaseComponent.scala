package hmda.query.dao

import slick.jdbc.JdbcBackend.Database

trait DatabaseComponent {
  val db: Database
}
