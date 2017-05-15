package hmda.validation.rules.lar.`macro`

import akka.pattern.ask
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.ValidationStats._
import org.scalacheck.Gen

import scala.concurrent.Await

class Q072Spec extends MacroSpecWithValidationStats {
  val threshold = configuration.getInt("hmda.validation.macro.Q072.currentYearThreshold")
  val proportionSold = configuration.getDouble("hmda.validation.macro.Q072.currentYearProportion")
  val yearDifference = configuration.getDouble("hmda.validation.macro.Q072.relativeProportion")

  "Q072" must {
    //// Check #1: comparing last year to this year ////
    val instId = "inst-with-prev-year-data"
    "set up: persist last year's data: sold 60% of loans" in {
      validationStats ! AddSubmissionMacroStats(SubmissionId(instId, "2016", 1), 0, 0, 0, 100, 60)
      val (relevant, relevantSold) = Await.result((validationStats ? FindQ072(instId, "2016")).mapTo[(Int, Int)], duration)
      relevant mustBe 100
      relevantSold mustBe 60
    }
    "(previous year check) pass when percentage sold is greater in current year than previous year" in {
      val numSold = (200 * (0.6 + yearDifference)).toInt
      val relevantNotSoldLars = listOfN(200 - numSold, Q072Spec.relevantNotSold)
      val relevantSoldLars = listOfN(numSold, Q072Spec.relevantSold)
      val irrelevantLars = listOfN(any, Q072Spec.irrelevant)
      val testLars = toSource(relevantSoldLars ++ relevantNotSoldLars ++ irrelevantLars)
      Q072.inContext(ctx(instId))(testLars).map(r => r mustBe a[Success])
    }
    s"(previous year check) pass when percentage sold is close enough to last year's percentage sold (within $yearDifference)" in {
      val numSold = (200 * (0.6 - yearDifference) + 1).toInt
      val relevantNotSoldLars = listOfN(200 - numSold, Q072Spec.relevantNotSold)
      val relevantSoldLars = listOfN(numSold, Q072Spec.relevantSold)
      val irrelevantLars = listOfN(any, Q072Spec.irrelevant)
      val testLars = toSource(relevantSoldLars ++ relevantNotSoldLars ++ irrelevantLars)
      Q072.inContext(ctx(instId))(testLars).map(r => r mustBe a[Success])
    }
    s"(previous year check) fail when percentage sold is too low compared to previous year ($yearDifference difference or more)" in {
      val numSold = (200 * (0.6 - yearDifference) - 1).toInt
      val relevantNotSoldLars = listOfN(200 - numSold, Q072Spec.relevantNotSold)
      val relevantSoldLars = listOfN(numSold, Q072Spec.relevantSold)
      val irrelevantLars = listOfN(any, Q072Spec.irrelevant)
      val testLars = toSource(relevantSoldLars ++ relevantNotSoldLars ++ irrelevantLars)
      Q072.inContext(ctx(instId))(testLars).map(r => r mustBe a[Failure])
    }

    //// Check #2: Current Year ////
    "(current year check) fails when too few relevant loans sold to Ginnie Mae" in {
      val instId = "first"
      val numSold: Int = (threshold * proportionSold).toInt
      val relevantNotSoldLars = listOfN(threshold - numSold, Q072Spec.relevantNotSold)
      val relevantSoldLars = listOfN(numSold, Q072Spec.relevantSold)
      val irrelevantLars = listOfN(any, Q072Spec.irrelevant)
      val testLars = toSource(relevantSoldLars ++ relevantNotSoldLars ++ irrelevantLars)
      Q072.inContext(ctx(instId))(testLars).map(r => r mustBe a[Failure])
    }
    "(current year check) passes when enough relevant loans sold to Ginnie Mae" in {
      val instId = "second"
      val numSold: Int = (threshold * proportionSold).toInt + 1
      val relevantNotSoldLars = listOfN(threshold - numSold, Q072Spec.relevantNotSold)
      val relevantSoldLars = listOfN(numSold, Q072Spec.relevantSold)
      val irrelevantLars = listOfN(any, Q072Spec.irrelevant)
      val testLars = toSource(relevantSoldLars ++ relevantNotSoldLars ++ irrelevantLars)
      Q072.inContext(ctx(instId))(testLars).map(r => r mustBe a[Success])
    }
    s"(current year check) passes when number of relevant loans is below $threshold" in {
      val instId = "third"
      val relevantSoldLars = listOfN(threshold - 1, Q072Spec.relevantSold)
      val irrelevantLars = listOfN(any, Q072Spec.irrelevant)
      val testLars = toSource(relevantSoldLars ++ irrelevantLars)
      Q072.inContext(ctx(instId))(testLars).map(r => r mustBe a[Success])
    }
    "(current year check) doesn't blow up when there are 0 relevant loans" in {
      val instId = "fourth"
      val irrelevantLarSource = toSource(listOfN(any, Q072Spec.irrelevant))
      Q072.inContext(ctx(instId))(irrelevantLarSource).map(r => r mustBe a[Success])
    }

    //// Must handle context correctly ////
    "be named Q072 when institution and year are present" in {
      Q072.inContext(ctx("Any")).name mustBe "Q072"
    }
    "be named 'empty' when institution or year is not present" in {
      val ctx1 = ValidationContext(None, Some(2017))
      Q072.inContext(ctx1).name mustBe "empty"

      val ctx2 = ValidationContext(Some(Institution.empty), None)
      Q072.inContext(ctx2).name mustBe "empty"
    }

  }
}

object Q072Spec {

  //// LAR transformation methods /////

  def irrelevant(lar: LoanApplicationRegister): LoanApplicationRegister = {
    lar.copy(actionTakenType = 2)
  }

  def relevantSold(lar: LoanApplicationRegister): LoanApplicationRegister = {
    relevant(lar).copy(purchaserType = 2)
  }

  def relevantNotSold(lar: LoanApplicationRegister): LoanApplicationRegister = {
    val purchaser = Gen.oneOf(0, 1, 3, 4, 5, 6, 7, 8, 9).sample.get
    relevant(lar).copy(purchaserType = purchaser)
  }

  private def relevant(lar: LoanApplicationRegister): LoanApplicationRegister = {
    val actionTaken = Gen.oneOf(1, 6).sample.get
    val purpose = Gen.oneOf(1, 3).sample.get
    val propType = Gen.oneOf(1, 2).sample.get

    val newLoan = lar.loan.copy(
      purpose = purpose,
      propertyType = propType,
      loanType = 3
    )

    lar.copy(loan = newLoan, actionTakenType = actionTaken)
  }

}
