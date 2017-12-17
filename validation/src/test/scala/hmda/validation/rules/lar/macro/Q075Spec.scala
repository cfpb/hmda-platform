package hmda.validation.rules.lar.`macro`

import akka.pattern.ask
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.validation.stats.ValidationStats.AddSubmissionMacroStats
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.messages.ValidationStatsMessages.FindQ075
import org.scalacheck.Gen

import scala.concurrent.Await

class Q075Spec extends MacroSpecWithValidationStats {
  val threshold = configuration.getInt("hmda.validation.macro.Q075.threshold") + 1
  val yearDifference = configuration.getDouble("hmda.validation.macro.Q075.relativeProportion")

  "Q075" must {

    val instId = "inst-with-prev-year-data"
    "set up: persist last year's data: sold 60% of loans" in {
      validationStats ! AddSubmissionMacroStats(SubmissionId(instId, "2016", 1), 0, 0, 0, 0, 0, 0, 0, 0.6, 0)
      val ratio = Await.result((validationStats ? FindQ075(instId, "2016")).mapTo[Double], duration)
      ratio mustBe 0.6
    }
    s"pass when current year's percentage sold is within -$yearDifference of prev year's" in {
      val numSold = (threshold * (0.6 - yearDifference) + 1).toInt
      val relevantNotSoldLars = listOfN(threshold - numSold, Q075Spec.relevantNotSold)
      val relevantSoldLars = listOfN(numSold, Q075Spec.relevantSold)
      val irrelevantLars = listOfN(any, Q075Spec.irrelevant)
      val testLars = toSource(relevantSoldLars ++ relevantNotSoldLars ++ irrelevantLars)
      Q075.inContext(ctx(instId))(testLars).map(r => r mustBe a[Success])
    }
    s"pass when current year's percentage sold is within +$yearDifference of prev year's" in {
      val numSold = (threshold * (0.6 + yearDifference) - 1).toInt
      val relevantNotSoldLars = listOfN(threshold - numSold, Q075Spec.relevantNotSold)
      val relevantSoldLars = listOfN(numSold, Q075Spec.relevantSold)
      val irrelevantLars = listOfN(any, Q075Spec.irrelevant)
      val testLars = toSource(relevantSoldLars ++ relevantNotSoldLars ++ irrelevantLars)
      Q075.inContext(ctx(instId))(testLars).map(r => r mustBe a[Success])
    }
    s"fail when percentage sold is too high compared to previous year ($yearDifference difference or more)" in {
      val numSold = (threshold * (0.6 + yearDifference) + 1).toInt
      val relevantNotSoldLars = listOfN(threshold - numSold, Q075Spec.relevantNotSold)
      val relevantSoldLars = listOfN(numSold, Q075Spec.relevantSold)
      val irrelevantLars = listOfN(any, Q075Spec.irrelevant)
      val testLars = toSource(relevantSoldLars ++ relevantNotSoldLars ++ irrelevantLars)
      Q075.inContext(ctx(instId))(testLars).map(r => r mustBe a[Failure])
    }
    s"fail when percentage sold is too low compared to previous year ($yearDifference difference or more)" in {
      val numSold = (threshold * (0.6 - yearDifference) - 1).toInt
      val relevantNotSoldLars = listOfN(threshold - numSold, Q075Spec.relevantNotSold)
      val relevantSoldLars = listOfN(numSold, Q075Spec.relevantSold)
      val irrelevantLars = listOfN(any, Q075Spec.irrelevant)
      val testLars = toSource(relevantSoldLars ++ relevantNotSoldLars ++ irrelevantLars)
      Q075.inContext(ctx(instId))(testLars).map(r => r mustBe a[Failure])
    }

    s"pass when number of relevant loans is below $threshold" in {
      val relevantSoldLars = listOfN(threshold - 1, Q075Spec.relevantSold)
      val irrelevantLars = listOfN(any, Q075Spec.irrelevant)
      val testLars = toSource(relevantSoldLars ++ irrelevantLars)
      Q075.inContext(ctx(instId))(testLars).map(r => r mustBe a[Success])
    }
    "doesn't blow up when there are 0 relevant loans" in {
      val irrelevantLarSource = toSource(listOfN(any, Q075Spec.irrelevant))
      Q075.inContext(ctx(instId))(irrelevantLarSource).map(r => r mustBe a[Success])
    }

    s"fail when >$threshold relevant loans and data is missing for previous year" in {
      val numSold = (threshold * (0.6 + yearDifference) - 1).toInt
      val relevantNotSoldLars = listOfN(threshold - numSold, Q075Spec.relevantNotSold)
      val relevantSoldLars = listOfN(numSold, Q075Spec.relevantSold)
      val irrelevantLars = listOfN(any, Q075Spec.irrelevant)
      val testLars = toSource(relevantSoldLars ++ relevantNotSoldLars ++ irrelevantLars)
      Q075.inContext(ctx("otherId"))(testLars).map(r => r mustBe a[Failure])
    }

    //// Must handle context correctly ////
    "be named Q075 when institution and year are present" in {
      Q075.inContext(ctx("Any")).name mustBe "Q075"
    }
    "be named 'empty' when institution or year is not present" in {
      val ctx1 = ValidationContext(None, Some(2017))
      Q075.inContext(ctx1).name mustBe "empty"

      val ctx2 = ValidationContext(Some(Institution.empty), None)
      Q075.inContext(ctx2).name mustBe "empty"
    }
  }

}

object Q075Spec {

  //// LAR transformation methods /////

  def irrelevant(lar: LoanApplicationRegister): LoanApplicationRegister = {
    lar.copy(actionTakenType = 2)
  }

  def relevantSold(lar: LoanApplicationRegister): LoanApplicationRegister = {
    val purchaser = Gen.oneOf(1, 2, 3, 4, 5, 6, 7, 8, 9).sample.get
    relevant(lar).copy(purchaserType = purchaser)
  }

  def relevantNotSold(lar: LoanApplicationRegister): LoanApplicationRegister = {
    relevant(lar).copy(purchaserType = 0)
  }

  private def relevant(lar: LoanApplicationRegister): LoanApplicationRegister = {
    val propType = Gen.oneOf(1, 2).sample.get
    val newLoan = lar.loan.copy(
      purpose = 1,
      propertyType = propType
    )

    val actionTaken = Gen.oneOf(1, 6).sample.get
    lar.copy(loan = newLoan, actionTakenType = actionTaken)
  }
}
