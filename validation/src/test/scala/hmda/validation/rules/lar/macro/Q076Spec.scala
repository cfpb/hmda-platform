package hmda.validation.rules.lar.`macro`

import akka.pattern.ask
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.validation.ValidationStats.{ AddSubmissionMacroStats, FindQ076 }
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.{ Failure, Success }
import org.scalacheck.Gen

import scala.concurrent.Await

class Q076Spec extends MacroSpecWithValidationStats {
  val threshold = configuration.getInt("hmda.validation.macro.Q076.threshold")
  val yearDifference = configuration.getDouble("hmda.validation.macro.Q076.relativeProportion")

  "Q076" must {

    val instId = "inst-with-prev-year-data"
    "set up: persist last year's data: sold 60% of loans" in {
      validationStats ! AddSubmissionMacroStats(SubmissionId(instId, "2016", 1), 0, 0, 0, 0, 0, 0, 0, 0, 0, 100, 60)
      val (relevant, relevantSold) = Await.result((validationStats ? FindQ076(instId, "2016")).mapTo[(Int, Int)], duration)
      relevant mustBe 100
      relevantSold mustBe 60
    }
    s"pass when current year's percentage sold is within -$yearDifference of prev year's" in {
      val numSold = (threshold * (0.6 - yearDifference) + 1).toInt
      val relevantNotSoldLars = listOfN(200 - numSold, Q076Spec.relevantNotSold)
      val relevantSoldLars = listOfN(numSold, Q076Spec.relevantSold)
      val irrelevantLars = listOfN(any, Q076Spec.irrelevant)
      val testLars = toSource(relevantSoldLars ++ relevantNotSoldLars ++ irrelevantLars)
      Q076.inContext(ctx(instId))(testLars).map(r => r mustBe a[Success])
    }
    s"pass when current year's percentage sold is within +$yearDifference of prev year's" in {
      val numSold = (threshold * (0.6 + yearDifference) - 1).toInt
      val relevantNotSoldLars = listOfN(200 - numSold, Q076Spec.relevantNotSold)
      val relevantSoldLars = listOfN(numSold, Q076Spec.relevantSold)
      val irrelevantLars = listOfN(any, Q076Spec.irrelevant)
      val testLars = toSource(relevantSoldLars ++ relevantNotSoldLars ++ irrelevantLars)
      Q076.inContext(ctx(instId))(testLars).map(r => r mustBe a[Success])
    }
    s"fail when percentage sold is too high compared to previous year ($yearDifference difference or more)" in {
      val numSold = (threshold * (0.6 + yearDifference) + 1).toInt
      val relevantNotSoldLars = listOfN(200 - numSold, Q076Spec.relevantNotSold)
      val relevantSoldLars = listOfN(numSold, Q076Spec.relevantSold)
      val irrelevantLars = listOfN(any, Q076Spec.irrelevant)
      val testLars = toSource(relevantSoldLars ++ relevantNotSoldLars ++ irrelevantLars)
      Q076.inContext(ctx(instId))(testLars).map(r => r mustBe a[Failure])
    }
    s"fail when percentage sold is too low compared to previous year ($yearDifference difference or more)" in {
      val numSold = (threshold * (0.6 - yearDifference) - 1).toInt
      val relevantNotSoldLars = listOfN(200 - numSold, Q076Spec.relevantNotSold)
      val relevantSoldLars = listOfN(numSold, Q076Spec.relevantSold)
      val irrelevantLars = listOfN(any, Q076Spec.irrelevant)
      val testLars = toSource(relevantSoldLars ++ relevantNotSoldLars ++ irrelevantLars)
      Q076.inContext(ctx(instId))(testLars).map(r => r mustBe a[Failure])
    }

    s"passes when number of relevant loans is below $threshold" in {
      val instId = "two"
      val relevantSoldLars = listOfN(threshold - 1, Q076Spec.relevantSold)
      val irrelevantLars = listOfN(any, Q076Spec.irrelevant)
      val testLars = toSource(relevantSoldLars ++ irrelevantLars)
      Q076.inContext(ctx(instId))(testLars).map(r => r mustBe a[Success])
    }
    "doesn't blow up when there are 0 relevant loans" in {
      val instId = "three"
      val irrelevantLarSource = toSource(listOfN(any, Q076Spec.irrelevant))
      Q076.inContext(ctx(instId))(irrelevantLarSource).map(r => r mustBe a[Success])
    }

    //// Must handle context correctly ////
    "be named Q076 when institution and year are present" in {
      Q076.inContext(ctx("Any")).name mustBe "Q076"
    }
    "be named 'empty' when institution or year is not present" in {
      val ctx1 = ValidationContext(None, Some(2017))
      Q076.inContext(ctx1).name mustBe "empty"

      val ctx2 = ValidationContext(Some(Institution.empty), None)
      Q076.inContext(ctx2).name mustBe "empty"
    }
  }

}

object Q076Spec {

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
      purpose = 3,
      propertyType = propType
    )

    val actionTaken = Gen.oneOf(1, 6).sample.get
    lar.copy(loan = newLoan, actionTakenType = actionTaken)
  }
}
