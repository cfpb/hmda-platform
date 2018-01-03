package hmda.api.http.public

import java.time.LocalDate

import akka.actor.ActorRef
import akka.pattern.ask
import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.api.http.FileUploadUtils
import hmda.model.apor.{ APOR, FixedRate, VariableRate }
import hmda.model.rateSpread.{ RateSpreadError, RateSpreadResponse }
import hmda.persistence.HmdaSupervisor
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import hmda.persistence.messages.commands.apor.APORCommands.{ CalculateRateSpread, CreateApor }
import hmda.api.protocol.apor.RateSpreadProtocol._
import hmda.persistence.HmdaSupervisor.FindAPORPersistence
import hmda.persistence.apor.HmdaAPORPersistence
import hmda.validation.stats.ValidationStats

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration._
import spray.json._

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
    val valuesFixed = Seq(3.01, 3.02, 3.03, 3.04, 3.05, 3.06, 3.07, 3.08, 3.09, 3.10, 3.11, 3.12, 3.13, 3.14, 3.15, 3.16, 3.17, 3.18, 3.19, 3.20, 3.21, 3.22, 3.23, 3.24, 3.25, 3.26, 3.27, 3.28, 3.29, 3.30, 3.31, 3.32, 3.33, 3.34, 3.35, 3.36, 3.37, 3.38, 3.39, 3.40, 3.41, 3.42, 3.43, 3.44, 3.45, 3.46, 3.47, 3.48, 3.49, 3.50)
    val valuesVariable = Seq(4.01, 4.02, 4.03, 4.04, 4.05, 4.06, 4.07, 4.08, 4.09, 4.10, 4.11, 4.12, 4.13, 4.14, 4.15, 4.16, 4.17, 4.18, 4.19, 4.20, 4.21, 4.22, 4.23, 4.24, 4.25, 4.26, 4.27, 4.28, 4.29, 4.30, 4.31, 4.32, 4.33, 4.34, 4.35, 4.36, 4.37, 4.38, 4.39, 4.40, 4.41, 4.42, 4.43, 4.44, 4.45, 4.46, 4.47, 4.48, 4.49, 4.50)
    aporPersistence ! CreateApor(APOR(LocalDate.of(2017, 11, 20), valuesFixed), FixedRate)
    aporPersistence ! CreateApor(APOR(LocalDate.of(2017, 11, 20), valuesVariable), VariableRate)
  }

  "APOR Calculator" must {
    val calculateFixedRateSpread = CalculateRateSpread(1, 30, FixedRate, 6.0, LocalDate.of(2017, 11, 21), 2)
    val calculateVariableRateSpread = CalculateRateSpread(1, 30, VariableRate, 6.0, LocalDate.of(2017, 11, 22), 2)

    ////////////////////////////////////
    // Individual Rate Spread Calculator
    ////////////////////////////////////

    "Calculate Rate Spread for Fixed term loan" in {
      Post("/rateSpread", calculateFixedRateSpread) ~> rateSpreadRoutes(supervisor) ~> check {
        status mustBe StatusCodes.OK
        responseAs[RateSpreadResponse].rateSpread mustBe "2.700"
      }
    }
    "Calculate Rate Spread for Variable term loan" in {
      Post("/rateSpread", calculateVariableRateSpread) ~> rateSpreadRoutes(supervisor) ~> check {
        status mustBe StatusCodes.OK
        responseAs[RateSpreadResponse].rateSpread mustBe "1.700"
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
        responseAs[RateSpreadError].message mustBe "Loan term must be 1-50"
      }

      val loanTerm51 = calculateVariableRateSpread.copy(loanTerm = 51)
      Post("/rateSpread", loanTerm51) ~> rateSpreadRoutes(supervisor) ~> check {
        status mustBe StatusCodes.BadRequest
        responseAs[RateSpreadError].message mustBe "Loan term must be 1-50"
      }
    }
    "Return 400 error if lock-in date isn't a valid date" in {
      val wrongDateRequest =
        s"""
           |{
           |  "actionTakenType": 1,
           |  "loanTerm": 30,
           |  "amortizationType": "FixedRate",
           |  "apr": 5.5,
           |  "lockInDate": "2017-13-30",
           |  "reverseMortgage": 2
           |}
         """.stripMargin.parseJson

      Post("/rateSpread", wrongDateRequest) ~> Route.seal(rateSpreadRoutes(supervisor)) ~> check {
        status mustBe StatusCodes.BadRequest
        response.entity.toString must include("Invalid value for MonthOfYear (valid values 1 - 12): 13")
      }
    }

    "Return 404 error if given lock-in date isn't in our APOR data" in {
      val lockIn1 = calculateVariableRateSpread.copy(lockInDate = LocalDate.of(2017, 10, 20))
      Post("/rateSpread", lockIn1) ~> rateSpreadRoutes(supervisor) ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[RateSpreadError].message mustBe "Cannot calculate rate spread; APOR value not found for lock-in date 2017-10-20"
      }

      val lockIn2 = calculateFixedRateSpread.copy(lockInDate = LocalDate.of(2018, 11, 20))
      Post("/rateSpread", lockIn2) ~> rateSpreadRoutes(supervisor) ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[RateSpreadError].message mustBe "Cannot calculate rate spread; APOR value not found for lock-in date 2018-11-20"
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
        csv must include(s"${calculateFixedRateSpread.toCSV},2.700")
        csv must include(s"${calculateVariableRateSpread.toCSV},1.700")
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
        csv must include(s"${lockIn1.toCSV},error: Cannot calculate rate spread; APOR value not found for lock-in date 2017-10-20")
        csv must include(s"${loanTerm51.toCSV},error: Loan term must be 1-50")
      }
    }

  }

}
