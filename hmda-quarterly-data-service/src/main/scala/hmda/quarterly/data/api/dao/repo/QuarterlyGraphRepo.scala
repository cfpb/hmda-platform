package hmda.quarterly.data.api.dao.repo

import hmda.model.filing.lar.enums.{ Conventional, LoanTypeEnum }
import hmda.quarterly.data.api.dao._
import monix.eval.Task
import slick.jdbc.{ PositionedParameters, SQLActionBuilder }
import QuarterlyGraphMvConfig._

object QuarterlyGraphRepo {
  import dbConfig._
  import dbConfig.profile.api._

  private val union = sql" union "
  private val ordering = sql" order by quarter"

  def fetchApplicationsVolumeByType(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = getAppVolStmts(APP_VOL_MV, APP_VOL_PERIODS, loanType, heloc, conforming)
    runStatements(stmts)
  }

  def fetchLoansVolumeByType(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = getAppVolStmts(APP_VOL_MV, APP_VOL_PERIODS, loanType, heloc, conforming, loanOriginated = true)
    runStatements(stmts)
  }

  private def getAppVolStmts(mv: String, periods: Seq[String], loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean,
                             loanOriginated: Boolean = false): Seq[SQLActionBuilder] =
    unionStatements(periods.map(period => {
      sql"""
         select last_updated, quarter, sum(agg) as value from #${s"${mv}_$period"}
         where #${if (heloc) "loc = 1" else s"lt = ${loanType.code} and loc != 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
           #${if (loanOriginated) "and action_taken_type = 1" else ""}
         group by last_updated, quarter
         """
    })) ++ Seq(ordering)

  def fetchTotalApplicationsVolume(quarterly: Boolean): Task[Seq[DataPoint]] = {
    val stmts = if (quarterly) getTotalAppVolStmts(APP_VOL_MV, APP_VOL_PERIODS) else getTotalAppVolStmts(ALL_APP_VOL_MV, ALL_APP_VOL_PERIODS)
    runStatements(stmts)
  }

  private def getTotalAppVolStmts(mv: String, periods: Seq[String]): Seq[SQLActionBuilder] =
    unionStatements(periods.map(period => {
      sql"""
         select last_updated, quarter, sum(agg) as value from #${s"${mv}_$period"}
         group by last_updated, quarter
         """
    })) ++ Seq(ordering)

  def fetchMedianCreditScoreByType(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(CRED_SCORE_BY_LOAN_PERIODS.map(period => {
      sql"""
         select last_updated, quarter, median_credit_score as value from #${s"${CRED_SCORE_BY_LOAN_MV}_$period"}
         where lt = ${loanType.code}
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianCreditScoreByTypeByRace(loanType: LoanTypeEnum, race: String, conforming: Boolean = false): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(CRED_SCORE_BY_RE_PERIODS.map(period => {
      sql"""
         select last_updated, quarter, median_credit_score as value from #${s"${CRED_SCORE_BY_RE_MV}_$period"}
         where lt = ${loanType.code} and race_ethnicity = $race
           #${getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianCLTVByType(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(CLTV_BY_LOAN_PERIODS.map(period => {
      sql"""
         select last_updated, quarter, median_lv as value from #${s"${CLTV_BY_LOAN_MV}_$period"}
         where lt = ${loanType.code}
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianCLTVByTypeByRace(loanType: LoanTypeEnum, race: String, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(CLTV_BY_RE_PERIODS.map(period => {
      sql"""
         select last_updated, quarter, median_lv as value from #${s"${CLTV_BY_RE_MV}_$period"}
         where lt = ${loanType.code} and race_ethnicity = $race
           and loc #${if (heloc) "= 1" else "!= 1"}
             #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianDTIByType(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(DTI_BY_LOAN_PERIODS.map(period => {
      sql"""
         select last_updated, quarter, median_dti as value from #${s"${DTI_BY_LOAN_MV}_$period"}
         where lt = ${loanType.code}
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianDTIByTypeByRace(loanType: LoanTypeEnum, race: String, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(DTI_BY_RE_PERIODS.map(period => {
      sql"""
         select last_updated, quarter, median_dti as value from #${s"${DTI_BY_RE_MV}_$period"}
         where lt = ${loanType.code} and race_ethnicity = $race
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchDenialRates(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(DENIAL_RATES_BY_LOAN_PERIODS.map(period => {
      sql"""
         select last_updated, quarter, denial_rate as value from #${s"${DENIAL_RATES_BY_LOAN_MV}_$period"}
         where lt = ${loanType.code}
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchDenialRatesByTypeByRace(loanType: LoanTypeEnum, race: String, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(DENIAL_RATES_BY_RE_PERIODS.map(period => {
      sql"""
         select last_updated, quarter, denial_rate as value from #${s"${DENIAL_RATES_BY_RE_MV}_$period"}
         where lt = ${loanType.code} and race_ethnicity = $race
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianInterestRates(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(INTEREST_RATES_BY_LOAN_PERIODS.map(period => {
      sql"""
         select last_updated, quarter, median_interest_rate as value from #${s"${INTEREST_RATES_BY_LOAN_MV}_$period"}
         where lt = ${loanType.code}
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianInterestRatesByTypeByRace(loanType: LoanTypeEnum, race: String, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(INTEREST_RATES_BY_RE_PERIODS.map(period => {
      sql"""
         select last_updated, quarter, median_interest_rate as value from #${s"${INTEREST_RATES_BY_RE_MV}_$period"}
         where lt = ${loanType.code} and race_ethnicity = $race
           and loc #${if (heloc) "= 1" else "!= 1"}
          #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianTotalLoanCosts(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(TLC_BY_LOAN_PERIODS.map(period => {
      sql"""
         select last_updated, quarter, median_total_loan_costs as value from #${s"${TLC_BY_LOAN_MV}_$period"}
         where lt = ${loanType.code}
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianTotalLoanCostsByTypeByRace(loanType: LoanTypeEnum, race: String, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(TLC_BY_RE_PERIODS.map(period => {
      sql"""
         select last_updated, quarter, median_total_loan_costs as value from #${s"${TLC_BY_RE_MV}_$period"}
         where lt = ${loanType.code} and race_ethnicity = $race
           and loc #${if (heloc) "= 1" else "!= 1"}
          #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  //home
  def fetchApplicationsVolumeByTypeLoanPurposeHome(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = getAppVolStmts(APP_VOL_MV, APP_VOL_PERIODS_PURPOSE_PURCHASE, loanType, heloc, conforming)
    runStatements(stmts)
  }

  def fetchLoansVolumeByTypeLoanPurposeHome(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = getAppVolStmts(APP_VOL_MV, APP_VOL_PERIODS_PURPOSE_PURCHASE, loanType, heloc, conforming, loanOriginated = true)
    runStatements(stmts)
  }



  def fetchTotalApplicationsVolumeLoanPurposeHome(quarterly: Boolean): Task[Seq[DataPoint]] = {
    val stmts = if (quarterly) getTotalAppVolStmts(APP_VOL_MV, APP_VOL_PERIODS_PURPOSE_PURCHASE) else getTotalAppVolStmts(ALL_APP_VOL_MV, ALL_APP_VOL_PERIODS_PURPOSE_PURCHASE)
    runStatements(stmts)
  }


  def fetchMedianCreditScoreByTypeLoanPurposeHome(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(CRED_SCORE_BY_LOAN_PERIODS_PURPOSE_PURCHASE.map(period => {
      sql"""
         select last_updated, quarter, median_credit_score as value from #${s"${CRED_SCORE_BY_LOAN_MV}_$period"}
         where lt = ${loanType.code}
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianCreditScoreByTypeByRaceLoanPurposeHome(loanType: LoanTypeEnum, race: String, conforming: Boolean = false): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(CRED_SCORE_BY_RE_PERIODS_PURPOSE_PURCHASE.map(period => {
      sql"""
         select last_updated, quarter, median_credit_score as value from #${s"${CRED_SCORE_BY_RE_MV}_$period"}
         where lt = ${loanType.code} and race_ethnicity = $race
           #${getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianCLTVByTypeLoanPurposeHome(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(CLTV_BY_LOAN_PERIODS_PURPOSE_PURCHASE.map(period => {
      sql"""
         select last_updated, quarter, median_lv as value from #${s"${CLTV_BY_LOAN_MV}_$period"}
         where lt = ${loanType.code}
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianCLTVByTypeByRaceLoanPurposeHome(loanType: LoanTypeEnum, race: String, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(CLTV_BY_RE_PERIODS_PURPOSE_PURCHASE.map(period => {
      sql"""
         select last_updated, quarter, median_lv as value from #${s"${CLTV_BY_RE_MV}_$period"}
         where lt = ${loanType.code} and race_ethnicity = $race
           and loc #${if (heloc) "= 1" else "!= 1"}
             #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianDTIByTypeLoanPurposeHome(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(DTI_BY_LOAN_PERIODS_PURPOSE_PURCHASE.map(period => {
      sql"""
         select last_updated, quarter, median_dti as value from #${s"${DTI_BY_LOAN_MV}_$period"}
         where lt = ${loanType.code}
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianDTIByTypeByRaceLoanPurposeHome(loanType: LoanTypeEnum, race: String, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(DTI_BY_RE_PERIODS_PURPOSE_PURCHASE.map(period => {
      sql"""
         select last_updated, quarter, median_dti as value from #${s"${DTI_BY_RE_MV}_$period"}
         where lt = ${loanType.code} and race_ethnicity = $race
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchDenialRatesLoanPurposeHome(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(DENIAL_RATES_BY_LOAN_PERIODS_PURPOSE_PURCHASE.map(period => {
      sql"""
         select last_updated, quarter, denial_rate as value from #${s"${DENIAL_RATES_BY_LOAN_MV}_$period"}
         where lt = ${loanType.code}
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchDenialRatesByTypeByRaceLoanPurposeHome(loanType: LoanTypeEnum, race: String, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(DENIAL_RATES_BY_RE_PERIODS_PURPOSE_PURCHASE.map(period => {
      sql"""
         select last_updated, quarter, denial_rate as value from #${s"${DENIAL_RATES_BY_RE_MV}_$period"}
         where lt = ${loanType.code} and race_ethnicity = $race
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianInterestRatesLoanPurposeHome(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(INTEREST_RATES_BY_LOAN_PERIODS_PURPOSE_PURCHASE.map(period => {
      sql"""
         select last_updated, quarter, median_interest_rate as value from #${s"${INTEREST_RATES_BY_LOAN_MV}_$period"}
         where lt = ${loanType.code}
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianInterestRatesByTypeByRaceLoanPurposeHome(loanType: LoanTypeEnum, race: String, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(INTEREST_RATES_BY_RE_PERIODS_PURPOSE_PURCHASE.map(period => {
      sql"""
         select last_updated, quarter, median_interest_rate as value from #${s"${INTEREST_RATES_BY_RE_MV}_$period"}
         where lt = ${loanType.code} and race_ethnicity = $race
           and loc #${if (heloc) "= 1" else "!= 1"}
          #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianTotalLoanCostsLoanPurposeHome(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(TLC_BY_LOAN_PERIODS_PURPOSE_PURCHASE.map(period => {
      sql"""
         select last_updated, quarter, median_total_loan_costs as value from #${s"${TLC_BY_LOAN_MV}_$period"}
         where lt = ${loanType.code}
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianTotalLoanCostsByTypeByRaceLoanPurposeHome(loanType: LoanTypeEnum, race: String, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(TLC_BY_RE_PERIODS_PURPOSE_PURCHASE.map(period => {
      sql"""
         select last_updated, quarter, median_total_loan_costs as value from #${s"${TLC_BY_RE_MV}_$period"}
         where lt = ${loanType.code} and race_ethnicity = $race
           and loc #${if (heloc) "= 1" else "!= 1"}
          #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

//refinance

  def fetchApplicationsVolumeByTypeLoanPurposeRefinance(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = getAppVolStmts(APP_VOL_MV, APP_VOL_PERIODS_PURPOSE_REFINANCE, loanType, heloc, conforming)
    runStatements(stmts)
  }

  def fetchLoansVolumeByTypeLoanPurposeRefinance(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = getAppVolStmts(APP_VOL_MV, APP_VOL_PERIODS_PURPOSE_REFINANCE, loanType, heloc, conforming, loanOriginated = true)
    runStatements(stmts)
  }



  def fetchTotalApplicationsVolumeLoanPurposeRefinance(quarterly: Boolean): Task[Seq[DataPoint]] = {
    val stmts = if (quarterly) getTotalAppVolStmts(APP_VOL_MV, APP_VOL_PERIODS_PURPOSE_REFINANCE) else getTotalAppVolStmts(ALL_APP_VOL_MV, ALL_APP_VOL_PERIODS_PURPOSE_REFINANCE)
    runStatements(stmts)
  }


  def fetchMedianCreditScoreByTypeLoanPurposeRefinance(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(CRED_SCORE_BY_LOAN_PERIODS_PURPOSE_REFINANCE.map(period => {
      sql"""
         select last_updated, quarter, median_credit_score as value from #${s"${CRED_SCORE_BY_LOAN_MV}_$period"}
         where lt = ${loanType.code}
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianCreditScoreByTypeByRaceLoanPurposeRefinance(loanType: LoanTypeEnum, race: String, conforming: Boolean = false): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(CRED_SCORE_BY_RE_PERIODS_PURPOSE_REFINANCE.map(period => {
      sql"""
         select last_updated, quarter, median_credit_score as value from #${s"${CRED_SCORE_BY_RE_MV}_$period"}
         where lt = ${loanType.code} and race_ethnicity = $race
           #${getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianCLTVByTypeLoanPurposeRefinance(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(CLTV_BY_LOAN_PERIODS_PURPOSE_REFINANCE.map(period => {
      sql"""
         select last_updated, quarter, median_lv as value from #${s"${CLTV_BY_LOAN_MV}_$period"}
         where lt = ${loanType.code}
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianCLTVByTypeByRaceLoanPurposeRefinance(loanType: LoanTypeEnum, race: String, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(CLTV_BY_RE_PERIODS_PURPOSE_REFINANCE.map(period => {
      sql"""
         select last_updated, quarter, median_lv as value from #${s"${CLTV_BY_RE_MV}_$period"}
         where lt = ${loanType.code} and race_ethnicity = $race
           and loc #${if (heloc) "= 1" else "!= 1"}
             #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianDTIByTypeLoanPurposeRefinance(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(DTI_BY_LOAN_PERIODS_PURPOSE_REFINANCE.map(period => {
      sql"""
         select last_updated, quarter, median_dti as value from #${s"${DTI_BY_LOAN_MV}_$period"}
         where lt = ${loanType.code}
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianDTIByTypeByRaceLoanPurposeRefinance(loanType: LoanTypeEnum, race: String, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(DTI_BY_RE_PERIODS_PURPOSE_REFINANCE.map(period => {
      sql"""
         select last_updated, quarter, median_dti as value from #${s"${DTI_BY_RE_MV}_$period"}
         where lt = ${loanType.code} and race_ethnicity = $race
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchDenialRatesLoanPurposeRefinance(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(DENIAL_RATES_BY_LOAN_PERIODS_PURPOSE_REFINANCE.map(period => {
      sql"""
         select last_updated, quarter, denial_rate as value from #${s"${DENIAL_RATES_BY_LOAN_MV}_$period"}
         where lt = ${loanType.code}
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchDenialRatesByTypeByRaceLoanPurposeRefinance(loanType: LoanTypeEnum, race: String, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(DENIAL_RATES_BY_RE_PERIODS_PURPOSE_REFINANCE.map(period => {
      sql"""
         select last_updated, quarter, denial_rate as value from #${s"${DENIAL_RATES_BY_RE_MV}_$period"}
         where lt = ${loanType.code} and race_ethnicity = $race
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianInterestRatesLoanPurposeRefinance(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(INTEREST_RATES_BY_LOAN_PERIODS_PURPOSE_REFINANCE.map(period => {
      sql"""
         select last_updated, quarter, median_interest_rate as value from #${s"${INTEREST_RATES_BY_LOAN_MV}_$period"}
         where lt = ${loanType.code}
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianInterestRatesByTypeByRaceLoanPurposeRefinance(loanType: LoanTypeEnum, race: String, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(INTEREST_RATES_BY_RE_PERIODS_PURPOSE_REFINANCE.map(period => {
      sql"""
         select last_updated, quarter, median_interest_rate as value from #${s"${INTEREST_RATES_BY_RE_MV}_$period"}
         where lt = ${loanType.code} and race_ethnicity = $race
           and loc #${if (heloc) "= 1" else "!= 1"}
          #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianTotalLoanCostsLoanPurposeRefinance(loanType: LoanTypeEnum, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(TLC_BY_LOAN_PERIODS_PURPOSE_REFINANCE.map(period => {
      sql"""
         select last_updated, quarter, median_total_loan_costs as value from #${s"${TLC_BY_LOAN_MV}_$period"}
         where lt = ${loanType.code}
           and loc #${if (heloc) "= 1" else "!= 1"}
           #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  def fetchMedianTotalLoanCostsByTypeByRaceLoanPurposeRefinance(loanType: LoanTypeEnum, race: String, heloc: Boolean, conforming: Boolean): Task[Seq[DataPoint]] = {
    val stmts = unionStatements(TLC_BY_RE_PERIODS_PURPOSE_REFINANCE.map(period => {
      sql"""
         select last_updated, quarter, median_total_loan_costs as value from #${s"${TLC_BY_RE_MV}_$period"}
         where lt = ${loanType.code} and race_ethnicity = $race
           and loc #${if (heloc) "= 1" else "!= 1"}
          #${if (heloc) "" else getAdditionalParams(loanType, conforming)}
         """
    })) ++ Seq(ordering)
    runStatements(stmts)
  }

  private def combineStatements(statements: SQLActionBuilder*): SQLActionBuilder =
    SQLActionBuilder(statements.map(_.queryParts).reduce(_ ++ _),
      (position: Unit, positionedParameters: PositionedParameters) => {
        statements.foreach(_.unitPConv.apply(position, positionedParameters))
      })

  private def unionStatements(statements: Seq[SQLActionBuilder]): Seq[SQLActionBuilder] =
    (for {
      statement <- statements
      unioned <- Seq(union, statement)
    } yield unioned).tail

  private def runStatements(statements: Seq[SQLActionBuilder]): Task[Vector[DataPoint]] = {
    val query = combineStatements(statements:_*).as[DataPoint]
    Task.deferFuture(db.run(query)).guarantee(Task.shift)
  }

  private def getAdditionalParams(loanType: LoanTypeEnum, conforming: Boolean): String =
    if (loanType == Conventional) {
      if (conforming) {
        "and cll = 'C'"
      } else {
        "and cll = 'NC'"
      }
    } else {
      ""
    }
}
