package hmda.calculator.apor

import org.scalatest.{ MustMatchers, WordSpec }
import hmda.calculator.api.RateSpreadResponse
import hmda.calculator.apor.APORCommands._
import hmda.calculator.apor.AporListEntity._

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AporCommandsSpec extends WordSpec with MustMatchers {
    val lockInDate =
        LocalDate.parse("2018-03-22", DateTimeFormatter.ISO_LOCAL_DATE)

    val exampleAPOR: APOR = APOR(
        lockInDate,
        Seq(1.01, 1.02, 1.03, 1.04, 1.05, 1.06, 1.07, 1.08, 1.09, 1.1, 1.11, 1.12, 1.13, 1.14, 1.15, 1.16, 1.17, 1.18, 1.19, 1.2, 1.21, 1.22,
            1.23, 1.24, 1.25, 1.26, 1.27, 1.28, 1.29, 1.3, 1.31, 1.32, 1.33, 1.34, 1.35, 1.36, 1.37, 1.38, 1.39, 1.40, 1.41, 1.42, 1.43, 1.44,
            1.45, 1.46, 1.47, 1.48, 1.49, 1.5)
    )
    exampleAPOR.toCSV
    AporOperation(exampleAPOR, FixedRate)
    AporOperation(
        APOR(
            lockInDate,
            Seq(2.01, 2.02, 2.03, 2.04, 2.05, 2.06, 2.07, 2.08, 2.09, 2.1, 2.11, 2.12, 2.13, 2.14, 2.15, 2.16, 2.17, 2.18, 2.19, 2.2, 2.21, 2.22,
                2.23, 2.24, 2.25, 2.26, 2.27, 2.28, 2.29, 2.3, 2.31, 2.32, 2.33, 2.34, 2.35, 2.36, 2.37, 2.38, 2.39, 2.40, 2.41, 2.42, 2.43, 2.44,
                2.45, 2.46, 2.47, 2.48, 2.49, 2.5)
        ),
        VariableRate
    )

    "Apor Commands" must {
        "error if loan term less than 1 year or greater than 50" in {
            getRateSpreadCalculation(1, -1, FixedRate, 3.0, lockInDate, 1) mustBe RateSpreadResponse("bad loan term")
            getRateSpreadCalculation(1, 51, FixedRate, 3.0, lockInDate, 1) mustBe RateSpreadResponse("bad loan term")
        }
        "return NA ratespread" in {
            getRateSpreadCalculation(1, 20, FixedRate, 3.0, lockInDate, 1) mustBe RateSpreadResponse("rate spread NA")
            getRateSpreadCalculation(3, 20, FixedRate, 3.0, lockInDate, 2) mustBe RateSpreadResponse("rate spread NA")
            getRateSpreadCalculation(4, 20, FixedRate, 3.0, lockInDate, 2) mustBe RateSpreadResponse("rate spread NA")
            getRateSpreadCalculation(5, 20, FixedRate, 3.0, lockInDate, 2) mustBe RateSpreadResponse("rate spread NA")
            getRateSpreadCalculation(6, 20, FixedRate, 3.0, lockInDate, 2) mustBe RateSpreadResponse("rate spread NA")
            getRateSpreadCalculation(7, 20, FixedRate, 3.0, lockInDate, 2) mustBe RateSpreadResponse("rate spread NA")
        }
        "return proper ratespread" in {
            getRateSpreadCalculation(1, 1, FixedRate, 3.0, lockInDate, 2) mustBe RateSpreadResponse("1.990")
            getRateSpreadCalculation(1, 50, VariableRate, 3.0, lockInDate, 2) mustBe RateSpreadResponse("0.500")
            getRateSpreadCalculation(1, 30, FixedRate, 3.0, lockInDate, 2) mustBe RateSpreadResponse("1.700")
            getRateSpreadCalculation(1, 30, VariableRate, 3.0, lockInDate, 2) mustBe RateSpreadResponse("0.700")

            getRateSpreadCalculation(2, 1, FixedRate, 3.0, lockInDate, 2) mustBe RateSpreadResponse("1.990")
            getRateSpreadCalculation(2, 50, VariableRate, 3.0, lockInDate, 2) mustBe RateSpreadResponse("0.500")
            getRateSpreadCalculation(8, 30, FixedRate, 3.0, lockInDate, 2) mustBe RateSpreadResponse("1.700")
            getRateSpreadCalculation(8, 30, VariableRate, 3.0, lockInDate, 2) mustBe RateSpreadResponse("0.700")
        }
    }
}