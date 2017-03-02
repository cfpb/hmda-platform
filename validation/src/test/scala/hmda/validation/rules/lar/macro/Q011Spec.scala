package hmda.validation.rules.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.model.institution.ExternalIdType.RssdId
import hmda.model.institution.{ ExternalId, Institution, Respondent }
import hmda.query.DbConfiguration
import hmda.query.model.filing.LoanApplicationRegisterQuery
import hmda.query.repository.filing.FilingComponent
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.{ Failure, Success }
import org.scalatest._
import hmda.query.repository.filing.LarConverter._
import org.scalacheck.Gen

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

class Q011Spec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll with FilingComponent with DbConfiguration {
  import config.profile.api._

  val configuration = ConfigFactory.load()
  val larSize = configuration.getInt("hmda.validation.macro.Q011.numOfTotalLars")
  val multiplier = configuration.getDouble("hmda.validation.macro.Q011.numOfLarsMultiplier")

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

  "Q011" must {
    val respId1 = "respId1"
    val resp1 = Respondent(ExternalId(respId1, RssdId), "", "", "", "")
    val currentYear = 2017
    "be named Q011 when institution and year are present" in {
      val ctx = ValidationContext(Some(Institution.empty.copy(respondent = resp1)), Some(currentYear))
      Q011.inContext(ctx).name mustBe "Q011"
    }
    "be named empty when institution is not present" in {
      val ctx = ValidationContext(None, Some(currentYear))
      Q011.inContext(ctx).name mustBe "empty"
    }
    "be named empty when year is not present" in {
      val ctx = ValidationContext(Some(Institution.empty), None)
      Q011.inContext(ctx).name mustBe "empty"
    }
    "succeed when number of last year and current year lars is less than configured value" in {
      val ctx = ValidationContext(Some(Institution.empty.copy(respondent = resp1)), Some(currentYear))
      val lastYear = currentYear - 1
      val n1 = Gen.choose(1, larSize - 1).sample.getOrElse(0)
      val n2 = Gen.choose(1, larSize - 1).sample.getOrElse(0)
      val lars = larNGen(n1).sample.getOrElse(List())
      val larSource = Source.fromIterator(() => lars.toIterator)
      val lastYearLars = larNGen(n2).sample.getOrElse(List()).map(lar => lar.copy(respondentId = respId1))
      val lastYearList = larsToLarQueryList(lastYearLars, respId1, lastYear)
      val resultF = Future.sequence(lastYearList.map(lar => repository.insertOrUpdate(lar)))
      Await.result(resultF, timeout)
      Q011.inContext(ctx)(larSource).map(r => r mustBe a[Success])
    }
  }



  private def larsToLarQueryList(lars: List[LoanApplicationRegister], respId: String, year: Int): List[LoanApplicationRegisterQuery] = {
    lars
      .map(lar => implicitly[LoanApplicationRegisterQuery](lar))
      .map(lar => lar.copy(period = year.toString))
  }

}
