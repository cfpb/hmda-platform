package hmda.analytics.query

import hmda.model.filing.submission.SubmissionId
import hmda.query.DbConfiguration.dbConfig
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

trait LarComponent2018 {
  import dbConfig.profile.api._

  class LarRepository2018(config: DatabaseConfig[JdbcProfile]) {

    val larTable = "loanapplicationregister2018"

    def deleteByLei(lei: String): Future[Int] = {
      config.db.run {
        sqlu"DELETE FROM #${larTable} WHERE UPPER(lei) = ${lei.toUpperCase}"
      }
    }

    def insert(le: LarEntity2018): Future[Int] =
      config.db.run {
        sqlu"""INSERT INTO #${larTable}
        VALUES (
          ${le.id},
          ${le.lei.toUpperCase},
          ${le.uli},
          ${le.appDate},
          ${le.loanType},
          ${le.loanPurpose},
          ${le.preapproval},
          ${le.constructionMethod},
          ${le.occupancyType},
          ${le.loanAmount},
          ${le.actionTakenType},
          ${le.actionTakenDate},
          ${le.street},
          ${le.city},
          ${le.state},
          ${le.zip},
          ${le.county},
          ${le.tract},
          ${le.ethnicityApplicant1},
          ${le.ethnicityApplicant2},
          ${le.ethnicityApplicant3},
          ${le.ethnicityApplicant4},
          ${le.ethnicityApplicant5},
          ${le.otherHispanicApplicant},
          ${le.ethnicityCoApplicant1},
          ${le.ethnicityCoApplicant2},
          ${le.ethnicityCoApplicant3},
          ${le.ethnicityCoApplicant4},
          ${le.ethnicityCoApplicant5},
          ${le.otherHispanicCoApplicant},
          ${le.ethnicityObservedApplicant},
          ${le.ethnicityObservedCoApplicant},
          ${le.raceApplicant1},
          ${le.raceApplicant2},
          ${le.raceApplicant3},
          ${le.raceApplicant4},
          ${le.raceApplicant5},
          ${le.otherNativeRaceApplicant},
          ${le.otherAsianRaceApplicant},
          ${le.otherPacificRaceApplicant},
          ${le.rateCoApplicant1},
          ${le.rateCoApplicant2},
          ${le.rateCoApplicant3},
          ${le.rateCoApplicant4},
          ${le.rateCoApplicant5},
          ${le.otherNativeRaceCoApplicant},
          ${le.otherAsianRaceCoApplicant},
          ${le.otherPacificRaceCoApplicant},
          ${le.raceObservedApplicant},
          ${le.raceObservedCoApplicant},
          ${le.sexApplicant},
          ${le.sexCoApplicant},
          ${le.observedSexApplicant},
          ${le.observedSexCoApplicant},
          ${le.ageApplicant},
          ${le.ageCoApplicant},
          ${le.income},
          ${le.purchaserType},
          ${le.rateSpread},
          ${le.hoepaStatus},
          ${le.lienStatus},
          ${le.creditScoreApplicant},
          ${le.creditScoreCoApplicant},
          ${le.creditScoreTypeApplicant},
          ${le.creditScoreModelApplicant},
          ${le.creditScoreTypeCoApplicant},
          ${le.creditScoreModelCoApplicant},
          ${le.denialReason1},
          ${le.denialReason2},
          ${le.denialReason3},
          ${le.denialReason4},
          ${le.otherDenialReason},
          ${le.totalLoanCosts},
          ${le.totalPoints},
          ${le.originationCharges},
          ${le.discountPoints},
          ${le.lenderCredits},
          ${le.interestRate},
          ${le.paymentPenalty},
          ${le.debtToIncome},
          ${le.loanValueRatio},
          ${le.loanTerm},
          ${le.rateSpreadIntro},
          ${le.baloonPayment},
          ${le.insertOnlyPayment},
          ${le.amortization},
          ${le.otherAmortization},
          ${le.propertyValues},
          ${le.homeSecurityPolicy},
          ${le.landPropertyInterest},
          ${le.totalUnits},
          ${le.mfAffordable},
          ${le.applicationSubmission},
          ${le.payable},
          ${le.nmls},
          ${le.aus1},
          ${le.aus2},
          ${le.aus3},
          ${le.aus4},
          ${le.aus5},
          ${le.otheraus},
          ${le.aus1Result},
          ${le.aus2Result},
          ${le.aus3Result},
          ${le.aus4Result},
          ${le.aus5Result},
          ${le.otherAusResult},
          ${le.reverseMortgage},
          ${le.lineOfCredits},
          ${le.businessOrCommercial}
        )
        """
      }
  }
}
