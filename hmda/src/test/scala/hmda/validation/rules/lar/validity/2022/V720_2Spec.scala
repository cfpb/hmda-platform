package hmda.validation.rules.lar.validity._2022

import hmda.model.filing.lar.LarGenerators.larGen
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

import scala.util.Random

class V720_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V720_2

  val relevantScoringModels = List(
    EquifaxBeacon5,
    ExperianFairIsaac,
    FICORiskScoreClassic04,
    FICORiskScoreClassic98,
    VantageScore2,
    VantageScore3,
    FICOScore9
  )
  val irrelevantScoringModels = List(
    OneOrMoreCreditScoreModels,
    OtherCreditScoreModel,
    CreditScoreNotApplicable,
    CreditScoreNoCoApplicant,
    CreditScoreExempt
  )

  property("if scoring model is 1, 2, 3, 4, 5, 6, or 11, credit score should be 280 and above") {
    forAll(larGen) { lar =>
      relevantScoringModels.foreach(scoringModel => {
        lar.copy(
          coApplicant = lar.coApplicant.copy(
            creditScoreType = scoringModel,
            creditScore = 279
          )
        ).mustFail
        lar.copy(
          coApplicant = lar.coApplicant.copy(
            creditScoreType = scoringModel,
            creditScore = 280
          )
        ).mustPass

        val irrelevantModel = irrelevantScoringModels(Random.nextInt(irrelevantScoringModels.size))
        lar.copy(
          coApplicant = lar.coApplicant.copy(
            creditScoreType = irrelevantModel,
            creditScore = 278
          )
        ).mustPass
      })
    }
  }
}