package hmda.utils

import java.nio.file.Paths
import java.util

import org.scalatest.{ BeforeAndAfterAll, Suite }
import ru.yandex.qatools.embed.postgresql.distribution.Version
import slick.basic.DatabaseConfig
import slick.dbio.DBIO
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration._

trait EmbeddedPostgres extends BeforeAndAfterAll { self: Suite =>
  private val embeddedPg = new ru.yandex.qatools.embed.postgresql.EmbeddedPostgres(Version.V10_6)
  val dbConfig           = DatabaseConfig.forConfig[JdbcProfile]("embedded-pg")

  def cleanupAction: DBIO[Int]

  def bootstrapSqlFile: String

  def loadSqlFileFromResources(s: String): Unit = {
    import dbConfig.profile.api._
    if (s.nonEmpty) {
      val db           = dbConfig.db
      val tableSchemas = scala.io.Source.fromResource(s).mkString
      Await.result(db.run(sql"#$tableSchemas".asUpdate), 30.seconds)
    } else ()
  }

  def cleanup(action: DBIO[Int]): Int = {
    val db = dbConfig.db
    Await.result(
      db.run(action),
      30.seconds
    )
  }

  override protected def beforeAll(): Unit = {
    val runtimeConfig    = ru.yandex.qatools.embed.postgresql.EmbeddedPostgres.cachedRuntimeConfig(Paths.get("/tmp/embedded-pg-data"))
    val additionalParams = new util.ArrayList[String](1)
    embeddedPg.start(runtimeConfig, "localhost", 5432, "postgres", "postgres", "postgres", additionalParams)
    cleanup(cleanupAction)
    loadSqlFileFromResources(bootstrapSqlFile)
    super.beforeAll()
  }

  override protected def afterAll(): Unit = {
    embeddedPg.close()
    super.afterAll()
  }
}