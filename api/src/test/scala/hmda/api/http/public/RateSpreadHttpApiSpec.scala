package hmda.api.http.public

import java.time.LocalDate

import akka.actor.ActorRef
import akka.pattern.ask
import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.api.http.FileUploadUtils
import hmda.api.model.public.RateSpreadModel.RateSpreadResponse
import hmda.model.apor.{ APOR, FixedRate, VariableRate }
import hmda.persistence.HmdaSupervisor
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import hmda.persistence.messages.commands.apor.APORCommands.{ CalculateRateSpread, CreateApor }
import hmda.validation.ValidationStats
import hmda.api.protocol.apor.RateSpreadProtocol._
import hmda.persistence.HmdaSupervisor.FindAPORPersistence
import hmda.persistence.apor.HmdaAPORPersistence

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration._

class RateSpreadHttpApiSpec extends WordSpec with MustMatchers with BeforeAndAfterAll
    with ScalatestRouteTest with RateSpreadHttpApi with FileUploadUtils {

  val duration = 10.seconds
  override implicit val timeout: Timeout = Timeout(duration)
  override val log: LoggingAdapter = NoLogging
  override val ec: ExecutionContext = system.dispatcher
  val config = ConfigFactory.load()
  override val parallelism = config.getInt("hmda.connectionFlowParallelism")

  val validationStats = ValidationStats.createValidationStats(system)
  val supervisor = HmdaSupervisor.createSupervisor(system, validationStats)
  val aporPersistenceF = (supervisor ? FindAPORPersistence(HmdaAPORPersistence.name)).mapTo[ActorRef]

  override def beforeAll(): Unit = {
    super.beforeAll()
    val aporPersistence = Await.result(aporPersistenceF, duration)
    val valuesFixed = Seq(3.20, 3.23, 3.23, 3.23, 3.37, 3.37, 3.52, 3.52, 3.60, 3.60, 3.60, 3.60, 3.38, 3.38, 3.38, 3.38, 3.38, 3.38, 3.38, 3.38, 3.38, 3.38, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99, 3.99)
    val valuesVariable = Seq(4.23, 4.14, 4.05, 4.05, 3.94, 3.94, 3.90, 3.90, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85, 3.85)
    aporPersistence ! CreateApor(APOR(LocalDate.of(2017, 11, 20), valuesFixed), FixedRate)
    aporPersistence ! CreateApor(APOR(LocalDate.of(2017, 11, 20), valuesVariable), VariableRate)
  }

  "APOR Calculator" must {
    val calculateFixedRateSpread = CalculateRateSpread(1, 30, FixedRate, 6.0, LocalDate.of(2017, 11, 20), 2)
    val calculateVariableRateSpread = CalculateRateSpread(1, 30, VariableRate, 6.0, LocalDate.of(2017, 11, 20), 2)
    val calculateBatchTxt = calculateFixedRateSpread.toCSV + "\n" +
      calculateVariableRateSpread.toCSV
    val rateSpreadFile = multiPartFile(calculateBatchTxt, "apor.txt")
    "Calculate Rate Spread for Fixed term loan" in {

      Post("/rateSpread", calculateFixedRateSpread) ~> rateSpreadRoutes(supervisor) ~> check {
        status mustBe StatusCodes.OK
        responseAs[RateSpreadResponse].rateSpread mustBe "2.01"
      }

    }
    "Calculate Rate Spread for Variable term loan" in {

      Post("/rateSpread", calculateVariableRateSpread) ~> rateSpreadRoutes(supervisor) ~> check {
        status mustBe StatusCodes.OK
        responseAs[RateSpreadResponse].rateSpread mustBe "2.15"
      }
    }

    "Return NA if reverse mortgage is 1" in {
      val calculateNAReverseMortgage = CalculateRateSpread(1, 30, VariableRate, 6.0, LocalDate.of(2017, 11, 20), 1)
      Post("/rateSpread", calculateNAReverseMortgage) ~> rateSpreadRoutes(supervisor) ~> check {
        status mustBe StatusCodes.OK
        responseAs[RateSpreadResponse].rateSpread mustBe "NA"
      }
    }

    "Return NA if action taken type is not 1, 2, or 8" in {
      val actionTakenTypes = List(3, 4, 5, 6, 7)
      for (actionTakenType <- actionTakenTypes) {
        val c = CalculateRateSpread(actionTakenType, 30, VariableRate, 6.0, LocalDate.of(2017, 11, 20), 2)
        Post("/rateSpread", c) ~> rateSpreadRoutes(supervisor) ~> check {
          status mustBe StatusCodes.OK
          responseAs[RateSpreadResponse].rateSpread mustBe "NA"
        }
      }
    }
    "Perform batch rate spread calculation on a file" in {
      Post("/rateSpread/csv", rateSpreadFile) ~> rateSpreadRoutes(supervisor) ~> check {
        status mustBe StatusCodes.OK
        val csv = responseAs[String]
        csv must include("action_taken_type,amortization_type,rate_type,apr,lockin_date,reverse_mortgage,rate_spread")
        csv must include(s"${calculateFixedRateSpread.toCSV},2.01")
        csv must include(s"${calculateVariableRateSpread.toCSV},2.15")

      }
    }
  }

}
