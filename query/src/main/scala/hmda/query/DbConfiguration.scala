package hmda.query

import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

trait DbConfiguration {
  lazy val config = DatabaseConfig.forConfig[JdbcProfile]("db")
}
