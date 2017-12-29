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
import hmda.api.model.public.RateSpreadModel.{ RateSpreadError, RateSpreadResponse }
import hmda.model.apor.{ APOR, FixedRate, VariableRate }
import hmda.persistence.HmdaSupervisor
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import hmda.persistence.messages.commands.apor.APORCommands.{ CalculateRateSpread, CreateApor }
import hmda.api.protocol.apor.RateSpreadProtocol._
import hmda.persistence.HmdaSupervisor.FindAPORPersistence
import hmda.persistence.apor.HmdaAPORPersistence
import hmda.validation.stats.ValidationStats

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

    ////////////////////////////////////
    // Individual Rate Spread Calculator
    ////////////////////////////////////

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
      val calculateNAReverseMortgage = calculateFixedRateSpread.copy(reverseMortgage = 1)
      Post("/rateSpread", calculateNAReverseMortgage) ~> rateSpreadRoutes(supervisor) ~> check {
        status mustBe StatusCodes.OK
        responseAs[RateSpreadResponse].rateSpread mustBe "NA"
      }
    }

    "Return NA if action taken type is not 1, 2, or 8" in {
      val actionTakenTypes = List(3, 4, 5, 6, 7)
      for (actionTakenType <- actionTakenTypes) {
        val c = calculateVariableRateSpread.copy(actionTakenType = actionTakenType)
        Post("/rateSpread", c) ~> rateSpreadRoutes(supervisor) ~> check {
          status mustBe StatusCodes.OK
          responseAs[RateSpreadResponse].rateSpread mustBe "NA"
        }
      }
    }
    "Return 400 error if loan term not in 1-50" in {
      val loanTerm0 = calculateFixedRateSpread.copy(loanTerm = 0)
      Post("/rateSpread", loanTerm0) ~> rateSpreadRoutes(supervisor) ~> check {
        status mustBe StatusCodes.BadRequest
        responseAs[RateSpreadError].error mustBe "Loan term must be 1-50"
      }

      val loanTerm51 = calculateVariableRateSpread.copy(loanTerm = 51)
      Post("/rateSpread", loanTerm51) ~> rateSpreadRoutes(supervisor) ~> check {
        status mustBe StatusCodes.BadRequest
        responseAs[RateSpreadError].error mustBe "Loan term must be 1-50"
      }
    }

    "Return 404 error if given lock-in date isn't in our APOR data" in {
      val lockIn1 = calculateVariableRateSpread.copy(lockInDate = LocalDate.of(2017, 10, 20))
      Post("/rateSpread", lockIn1) ~> rateSpreadRoutes(supervisor) ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[RateSpreadError].error mustBe "Cannot calculate rate spread; APOR value not found for lock-in date 2017-10-20"
      }

      val lockIn2 = calculateFixedRateSpread.copy(lockInDate = LocalDate.of(2018, 11, 20))
      Post("/rateSpread", lockIn2) ~> rateSpreadRoutes(supervisor) ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[RateSpreadError].error mustBe "Cannot calculate rate spread; APOR value not found for lock-in date 2018-11-20"
      }
    }

    ////////////////////////////////
    // Batch Rate Spread Calculator
    ////////////////////////////////

    val calculateBatchTxt = calculateFixedRateSpread.toCSV + "\n" + calculateVariableRateSpread.toCSV
    val rateSpreadFile = multiPartFile(calculateBatchTxt, "apor.txt")

    "Perform batch rate spread calculation on a file" in {
      Post("/rateSpread/csv", rateSpreadFile) ~> rateSpreadRoutes(supervisor) ~> check {
        status mustBe StatusCodes.OK
        val csv = responseAs[String]
        csv must include("action_taken_type,loan_term,amortization_type,apr,lock_in_date,reverse_mortgage,rate_spread")
        csv must include(s"${calculateFixedRateSpread.toCSV},2.01")
        csv must include(s"${calculateVariableRateSpread.toCSV},2.15")
      }
    }

    val badCSV = "non,sense" + "\n" + "bogus,rate,spread,csv"
    val badCSVFile = multiPartFile(badCSV, "nonsense.txt")
    "Return error for invalid rate spread CSV" in {
      Post("/rateSpread/csv", badCSVFile) ~> rateSpreadRoutes(supervisor) ~> check {
        status mustBe StatusCodes.OK
        val csv = responseAs[String]
        csv must include("action_taken_type,loan_term,amortization_type,apr,lock_in_date,reverse_mortgage,rate_spread")
        csv must include(s"non,sense,error: invalid rate spread CSV")
        csv must include(s"bogus,rate,spread,csv,error: invalid rate spread CSV")
      }
    }

    val lockIn1 = calculateVariableRateSpread.copy(lockInDate = LocalDate.of(2017, 10, 20))
    val loanTerm51 = calculateVariableRateSpread.copy(loanTerm = 51)
    val reverseMortgageNA = calculateFixedRateSpread.copy(reverseMortgage = 1)
    val batchWithErrors = List(reverseMortgageNA, lockIn1, loanTerm51).map(_.toCSV).mkString("\n")
    val rateSpreadFileWithErrors = multiPartFile(batchWithErrors, "apor-err.txt")

    "Put error and NA responses in line" in {
      Post("/rateSpread/csv", rateSpreadFileWithErrors) ~> rateSpreadRoutes(supervisor) ~> check {
        status mustBe StatusCodes.OK
        val csv = responseAs[String]
        csv must include("action_taken_type,loan_term,amortization_type,apr,lock_in_date,reverse_mortgage,rate_spread")
        csv must include(s"${reverseMortgageNA.toCSV},NA")
        csv must include(s"${lockIn1.toCSV},error: Cannot calculate rate spread; APOR value not found for lock-in date 2018-11-20")
        csv must include(s"${loanTerm51.toCSV},error: Loan term must be 1-50")
      }
    }

  }

}
