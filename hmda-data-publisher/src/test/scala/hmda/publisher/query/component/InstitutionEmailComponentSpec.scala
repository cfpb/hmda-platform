package hmda.publisher.query.component

import hmda.publisher.query.panel.InstitutionEmailEntity
import hmda.utils.EmbeddedPostgres
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach, FreeSpec, Matchers }
import org.scalatest.concurrent.{ PatienceConfiguration, ScalaFutures }
import org.scalatest.time.{ Millis, Minutes, Span }

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._

class InstitutionEmailComponentSpec
  extends FreeSpec
    with InstitutionEmailComponent
    with EmbeddedPostgres
    with ScalaFutures
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with PatienceConfiguration {

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(2, Minutes), interval = Span(100, Millis))

  import dbConfig.profile.api._
  val repo = new InstitutionEmailsRepository2018(dbConfig)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.ready(repo.createSchema(), 30.seconds)
  }

  override def afterEach(): Unit =
    Await.ready(dbConfig.db.run(repo.table.delete), 30.seconds)

  override def afterAll(): Unit = {
    Await.ready(repo.dropSchema(), 30.seconds)
    super.afterAll()
  }

  override def bootstrapSqlFile: String = ""

  "getId returns the Rep[Int]" in {
    repo.getId(_)
  }

  "deleteById deletes ids" in {
    val test = for {
      id        <- dbConfig.db.run(repo.table += InstitutionEmailEntity(lei = "EXAMPLE", emailDomain = "example@domain.com"))
      deletedId <- repo.deleteById(id)
    } yield id shouldBe deletedId
    whenReady(test)(_ => ())
  }

  "findByLei finds LEIs" in {
    val data = InstitutionEmailEntity(lei = "EXAMPLE", emailDomain = "example@domain.com")
    val test = for {
      _      <- dbConfig.db.run(repo.table += data)
      result <- repo.findByLei(data.lei)
    } yield {
      result should have length 1
      result.map(_.lei) should contain theSameElementsAs List(data.lei)
    }
    whenReady(test)(_ => ())
  }

  "getAllDomains finds records by domain" in {
    val data = InstitutionEmailEntity(lei = "EXAMPLE2", emailDomain = "example@domain1.com")
    val test = for {
      _      <- dbConfig.db.run(repo.table += data)
      result <- repo.getAllDomains()
    } yield {
      result should have length 1
      result.map(_.lei) should contain theSameElementsAs List(data.lei)
    }
    whenReady(test)(_ => ())
  }
}