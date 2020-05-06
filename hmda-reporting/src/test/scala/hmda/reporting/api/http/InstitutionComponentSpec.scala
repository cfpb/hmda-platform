package hmda.reporting.repository

import hmda.query.institution.InstitutionEntity
import hmda.utils.EmbeddedPostgres
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._

class InstitutionComponentSpec extends WordSpec with EmbeddedPostgres with InstitutionComponent with ScalaFutures with Matchers {
  import dbConfig._
  import dbConfig.profile.api._

  val institutionRepo = new InstitutionRepository(dbConfig, "institutions_table")

  override def bootstrapSqlFile: String = ""

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.ready(institutionRepo.createSchema(), 30.seconds)
  }

  override def afterAll(): Unit = {
    Await.ready(institutionRepo.dropSchema(), 30.seconds) // just for test coverage
    super.afterAll()
  }

  "InstitutionRepository run-through" in {
    whenReady(db.run(institutionRepo.table += InstitutionEntity("EXAMPLE-LEI-1", activityYear = 2018, hmdaFiler = true)))(_ shouldBe 1)

    val test = for {
      result <- institutionRepo.findByLei("EXAMPLE-LEI-1")
      _      = result should have length 1
      result <- institutionRepo.getAllFilers()
      _      = result should have length 1
      _      <- institutionRepo.getFilteredFilers(Array.empty)
      _      = result should have length 1
      _      <- institutionRepo.deleteById("EXAMPLE-LEI-1")
      _      = institutionRepo.getId(institutionRepo.table.baseTableRow)
    } yield ()

    whenReady(test)(_ => ())
  }
}