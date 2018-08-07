package hmda.query

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

object DbConfiguration {
  val config = DatabaseConfig.forConfig[JdbcProfile]("db")
}
