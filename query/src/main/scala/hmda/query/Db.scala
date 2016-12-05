package hmda.query

import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

trait Db {
  val config: DatabaseConfig[JdbcProfile]
  val db: JdbcProfile#Backend#Database = config.db
}
