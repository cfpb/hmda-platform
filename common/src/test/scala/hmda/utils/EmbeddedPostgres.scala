package hmda.utils

import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach, Suite }
import ru.yandex.qatools.embed.postgresql.distribution.Version
import slick.basic.DatabaseConfig
import slick.dbio.DBIO
import slick.jdbc.JdbcProfile
import slick.jdbc.meta.MTable

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Embedded Postgres loads up a file (if you want) at start up and automatically truncates tables in the public schema
 * after each test and finally deletes all tables before the suite is complete and tears down the database
 */
trait EmbeddedPostgres extends BeforeAndAfterAll with BeforeAndAfterEach { self: Suite =>
  private val embeddedPg = new ru.yandex.qatools.embed.postgresql.EmbeddedPostgres(Version.V10_6)
  val dbConfig           = DatabaseConfig.forConfig[JdbcProfile]("embedded-pg")

  def bootstrapSqlFile: String

  def loadSqlFileFromResources(s: String): Unit = {
    import dbConfig.profile.api._
    if (s.nonEmpty) {
      val db           = dbConfig.db
      val tableSchemas = scala.io.Source.fromResource(s).mkString
      Await.result(db.run(sql"#$tableSchemas".asUpdate), 30.seconds)
    } else ()
  }

  def executeSQL(action: DBIO[Int]): Int = {
    val db = dbConfig.db
    Await.result(
      db.run(action),
      30.seconds
    )
  }

  private val removeAllTables = {
    import dbConfig.profile.api._
    sql"DROP SCHEMA public CASCADE".asUpdate >>
      sql"CREATE SCHEMA public".asUpdate >>
      sql"GRANT ALL ON SCHEMA public TO postgres".asUpdate >>
      sql"GRANT ALL ON SCHEMA public TO public".asUpdate
  }

  override protected def beforeAll(): Unit = {
    embeddedPg.start("localhost", 5432, "postgres", "postgres", "postgres")
    executeSQL(removeAllTables)
    loadSqlFileFromResources(bootstrapSqlFile)
    super.beforeAll()
  }

  override protected def afterAll(): Unit = {
    executeSQL(removeAllTables)
    embeddedPg.close()
    super.afterAll()
  }

  override def afterEach(): Unit = {
    import dbConfig.profile.api._
    executeSQL {
      for {
        tables <- MTable.getTables(cat = None, schemaPattern = Some("public"), namePattern = None, types = None)
        _ <- dbConfig.profile.api.DBIO.sequence(
          tables
            .filter(_.tableType == "TABLE")
            .map(_.name.name)
            .map(tableName => sql"TRUNCATE #$tableName".asUpdate)
        )
      } yield 1
    }
    super.afterEach()
  }
}