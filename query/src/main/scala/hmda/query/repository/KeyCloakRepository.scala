package hmda.query.repository

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.H2Profile.api._

import scala.concurrent.Future

trait KeyCloakRepository {
  val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("db")
  val db = dbConfig.db

  def findEmailsById(id: String): Future[Seq[(String, String, String)]] = {
    val query =
      sql"""SELECT ue.first_name, ue.last_name, ue.email
         FROM user_entity AS ue
            INNER JOIN user_attribute AS ua ON ua.user_id = ue.id
         WHERE ue.realm_id = 'hmda'
            AND ue.enabled = 't'
            AND ue.email_verified = 't'
            AND ua.name = 'institutions'
            AND (ua.value = '#$id'
              OR ua.value LIKE '#$id,%'
              OR ua.value LIKE '%,#$id'
              OR ua.value LIKE '%,#$id,%')"""
    db.run(query.as[(String, String, String)])
  }
}
