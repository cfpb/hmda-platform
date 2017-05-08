package hmda.validation.rules.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionGenerators.sampleInstitution
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, MustMatchers }

class Q071Spec extends AsyncWordSpec with MustMatchers with LarGenerators {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit override def executionContext = system.dispatcher

  val configuration = ConfigFactory.load()
  val threshold = configuration.getInt("hmda.validation.macro.Q071.currentYearThreshold")
  val proportionSold = configuration.getDouble("hmda.validation.macro.Q071.currentYearProportion")
  val yearDifference = configuration.getDouble("hmda.validation.macro.Q071.relativeProportion")
  def any: Int = Gen.choose(0, 100).sample.get

  "Q071" must {
    val currentYear = 2017

    //// Check #1: comparing last year to this year ////
    "fetch count of previous year's relevant loans" in {

      true mustBe true
    }

    //// Check #2: Current Year ////
    "current year: fails when too few relevant loans sold to Ginnie Mae" in {
      val instId = "first"
      val numSold: Int = (threshold * proportionSold).toInt
      val relevantNotSoldLars = listOfN(threshold - numSold, relevantNotSold)
      val relevantSoldLars = listOfN(numSold, relevantSold)
      val irrelevantLars = listOfN(any, irrelevant)
      val testLars = toSource(relevantSoldLars ++ relevantNotSoldLars ++ irrelevantLars)
      Q071.inContext(ctx(instId))(testLars).map(r => r mustBe a[Failure])
    }
    "current year: passes when enough relevant loans sold to Ginnie Mae" in {
      val instId = "second"
      val numSold: Int = (threshold * proportionSold).toInt + 1
      val relevantNotSoldLars = listOfN(threshold - numSold, relevantNotSold)
      val relevantSoldLars = listOfN(numSold, relevantSold)
      val irrelevantLars = listOfN(any, irrelevant)
      val testLars = toSource(relevantSoldLars ++ relevantNotSoldLars ++ irrelevantLars)
      Q071.inContext(ctx(instId))(testLars).map(r => r mustBe a[Success])
    }
    s"current year: passes when number of relevant loans is below $threshold" in {
      val instId = "third"
      val relevantSoldLars = listOfN(threshold - 1, relevantSold)
      val irrelevantLars = listOfN(any, irrelevant)
      val testLars = toSource(relevantSoldLars ++ irrelevantLars)
      Q071.inContext(ctx(instId))(testLars).map(r => r mustBe a[Success])
    }

    //// Must handle context correctly ////
    "be named Q071 when institution and year are present" in {
      Q071.inContext(ctx("Any")).name mustBe "Q071"
    }

    "be named 'empty' when institution or year is not present" in {
      val ctx1 = ValidationContext(None, Some(currentYear))
      Q071.inContext(ctx1).name mustBe "empty"

      val ctx2 = ValidationContext(Some(Institution.empty), None)
      Q071.inContext(ctx2).name mustBe "empty"
    }

  }

  private def ctx(institutionId: String, currentYear: Int = 2017): ValidationContext = {
    ValidationContext(Some(sampleInstitution.copy(id = institutionId)), Some(currentYear))
  }

  private def toSource(lars: List[LoanApplicationRegister]): LoanApplicationRegisterSource = {
    Source.fromIterator(() => lars.toIterator)
  }

  private def listOfN(n: Int, transform: LoanApplicationRegister => LoanApplicationRegister): List[LoanApplicationRegister] = {
    larNGen(n).sample.getOrElse(List()).map(transform)
  }

  //// LAR transformation methods /////

  private def irrelevant(lar: LoanApplicationRegister): LoanApplicationRegister = {
    lar.copy(actionTakenType = 2)
  }

  private def relevantSold(lar: LoanApplicationRegister): LoanApplicationRegister = {
    relevant(lar).copy(purchaserType = 2)
  }

  private def relevantNotSold(lar: LoanApplicationRegister): LoanApplicationRegister = {
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
      loanType = 2
    )

    lar.copy(loan = newLoan, actionTakenType = actionTaken)
  }

}
