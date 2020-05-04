package hmda.analytics.query

import java.nio.file.Paths
import java.util

import org.scalatest.{BeforeAndAfterAll, Suite}
import ru.yandex.qatools.embed.postgresql.distribution.Version
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration._

trait EmbeddedPostgres extends BeforeAndAfterAll { self: Suite =>
  private val embeddedPg = new ru.yandex.qatools.embed.postgresql.EmbeddedPostgres(Version.V10_6)
  val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("embedded-pg")

  override protected def beforeAll(): Unit = {
    val runtimeConfig = ru.yandex.qatools.embed.postgresql.EmbeddedPostgres.cachedRuntimeConfig(Paths.get("/tmp/embedded-pg-data"))
    val additionalParams = new util.ArrayList[String](1)
    embeddedPg.start(runtimeConfig, "localhost", 5432, "postgres", "postgres", "postgres", additionalParams)
    embeddedPg.start()

    import dbConfig.profile.api._
    val db = dbConfig.db

    Await.result(
      db.run(
        sql"""
      DROP SCHEMA public CASCADE;
      CREATE SCHEMA public;
      GRANT ALL ON SCHEMA public TO postgres;
      GRANT ALL ON SCHEMA public TO public;
      """.asUpdate),
      30.seconds
    )

    val tableSchemas = scala.io.Source.fromResource("hmda.sql").mkString
    Await.result(db.run(sql"#$tableSchemas".asUpdate), 30.seconds)
  }

  override protected def afterAll(): Unit = {
    embeddedPg.close()
  }
}