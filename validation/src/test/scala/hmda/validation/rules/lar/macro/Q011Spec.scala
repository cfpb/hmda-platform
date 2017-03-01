package hmda.validation.rules.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LarGenerators
import hmda.model.institution.Institution
import hmda.query.DbConfiguration
import hmda.query.repository.filing.{ FilingComponent, FilingComponentSpec }
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.{ Failure, Success }
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers, PropSpec }

import scala.concurrent.Await
import scala.concurrent.duration._

class Q011Spec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll with FilingComponent with DbConfiguration {
  import config.profile.api._

  val configuration = ConfigFactory.load()
  val larSize = configuration.getInt("hmda.validation.macro.Q011.lar.size")
  val multiplier = configuration.getInt("hmda.validation.macro.Q011.lar.multiplier")

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit override def executionContext = system.dispatcher

  val repository = new LarRepository(config)
  val modifiedLarRepository = new ModifiedLarRepository(config)
  val totalLarRepository = new LarTotalRepository(config)

  implicit val timeout = 5.seconds

  override def beforeAll(): Unit = {
    super.beforeAll()
    dropAllObjects()
    Await.result(repository.createSchema(), timeout)
    Await.result(modifiedLarRepository.createSchema(), timeout)
    Await.result(totalLarRepository.createSchema(), timeout)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    dropAllObjects()
    repository.config.db.close()
    system.terminate()
  }

  private def dropAllObjects() = {
    val db = repository.config.db
    val dropAll = sqlu"""DROP ALL OBJECTS"""
    Await.result(db.run(dropAll), timeout)
  }

  "Q011" when {
    "institution and year are present" must {
      "be named Q011" in {
        val ctx = ValidationContext(Some(Institution.empty), Some(2017))
        Q011.inContext(ctx).name mustBe "Q011"
      }
      "succeed if last year and current year lar size is less than configured value" in {
        val ctx = ValidationContext(Some(Institution.empty.copy("12345")), Some(2017))
        val lars = lar100ListGen.sample.getOrElse(List())
        val larSource = Source.fromIterator(() => lars.toIterator)
        Q011.inContext(ctx)(larSource).map(r => r mustBe a[Success])
      }
    }
    "institution is not present" must {
      val ctx = ValidationContext(None, Some(2017))
      "be named empty" in {
        Q011.inContext(ctx).name mustBe "empty"
      }
    }
    "year is not present" must {
      "be named empty" in {
        val ctx = ValidationContext(Some(Institution.empty), None)
        Q011.inContext(ctx).name mustBe "empty"
      }
    }
  }

}
