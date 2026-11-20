package hmda.reporting.repository

import hmda.query.institution.InstitutionEntity
import hmda.query.repository.InstitutionComponent
import hmda.utils.EmbeddedPostgres
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._

class InstitutionComponentSpec extends WordSpec with EmbeddedPostgres with InstitutionComponent with ScalaFutures with Matchers {
  import dbConfig._
  import dbConfig.profile.api._

  val institutionRepo = new InstitutionRepository(dbConfig)

  override def bootstrapSqlFile: String = ""

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.ready(institutionRepo.createSchema(2018), 30.seconds)
  }

  "InstitutionRepository run-through" in {
    whenReady(db.run(institutionRepo.getYearTable(2018, false) += InstitutionEntity("EXAMPLE-LEI-1", activityYear = 2018, hmdaFiler = true)))(_ shouldBe 1)

    val test = for {
      result <- institutionRepo.findByLei("EXAMPLE-LEI-1", 2018, false)
      _      = result should have length 1
      result <- institutionRepo.getAllFilers(2018, false)
      _      = result should have length 1
      _      <- institutionRepo.getFilteredFilers(Array.empty, 2018, false)
      _      = result should have length 1
    } yield ()

    whenReady(test)(_ => ())
  }
}