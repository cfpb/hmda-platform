package hmda.quarterly.data.api.dao.repo

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

object QuarterlyGraphMvConfig {
  val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile]("db")
  import dbConfig._
  val APP_VOL_MV: String = config.getString("mv.app_vol")
  val APP_VOL_PERIODS: Seq[String] = config.getString("mv.periods").split(",").toSeq
  val ALL_APP_VOL_MV: String = config.getString("mv.all_app_vol")
  val ALL_APP_VOL_PERIODS: Seq[String] = config.getString("mv.all_app_vol_periods").split(",").toSeq
  val CRED_SCORE_BY_LOAN_MV: String = config.getString("mv.cred_score_by_loan")
  val CRED_SCORE_BY_LOAN_PERIODS: Seq[String] = config.getString("mv.periods").split(",").toSeq
  val CRED_SCORE_BY_RE_MV: String = config.getString("mv.cred_score_by_re")
  val CRED_SCORE_BY_RE_PERIODS: Seq[String] = config.getString("mv.periods").split(",").toSeq
  val CLTV_BY_LOAN_MV: String = config.getString("mv.cltv_by_loan")
  val CLTV_BY_LOAN_PERIODS: Seq[String] = config.getString("mv.periods").split(",").toSeq
  val CLTV_BY_RE_MV: String = config.getString("mv.cltv_by_re")
  val CLTV_BY_RE_PERIODS: Seq[String] = config.getString("mv.periods").split(",").toSeq
  val DTI_BY_LOAN_MV: String = config.getString("mv.dti_by_loan")
  val DTI_BY_LOAN_PERIODS: Seq[String] = config.getString("mv.periods").split(",").toSeq
  val DTI_BY_RE_MV: String = config.getString("mv.dti_by_re")
  val DTI_BY_RE_PERIODS: Seq[String] = config.getString("mv.periods").split(",").toSeq
  val DENIAL_RATES_BY_LOAN_MV: String = config.getString("mv.denial_rates_by_loan")
  val DENIAL_RATES_BY_LOAN_PERIODS: Seq[String] = config.getString("mv.periods").split(",").toSeq
  val DENIAL_RATES_BY_RE_MV: String = config.getString("mv.denial_rates_by_re")
  val DENIAL_RATES_BY_RE_PERIODS: Seq[String] = config.getString("mv.periods").split(",").toSeq
  val INTEREST_RATES_BY_LOAN_MV: String = config.getString("mv.interest_rates_by_loan")
  val INTEREST_RATES_BY_LOAN_PERIODS: Seq[String] = config.getString("mv.periods").split(",").toSeq
  val INTEREST_RATES_BY_RE_MV: String = config.getString("mv.interest_rates_by_re")
  val INTEREST_RATES_BY_RE_PERIODS: Seq[String] = config.getString("mv.periods").split(",").toSeq
  val TLC_BY_LOAN_MV: String = config.getString("mv.tlc_by_loan")
  val TLC_BY_LOAN_PERIODS: Seq[String] = config.getString("mv.periods").split(",").toSeq
  val TLC_BY_RE_MV: String = config.getString("mv.tlc_by_re")
  val TLC_BY_RE_PERIODS: Seq[String] = config.getString("mv.periods").split(",").toSeq
  // loan purpose home
  val APP_VOL_PERIODS_PURPOSE_PURCHASE: Seq[String] = config.getString("mv.app_vol_periods_purpose_p_suffix").split(",").toSeq
  val ALL_APP_VOL_PERIODS_PURPOSE_PURCHASE: Seq[String] = config.getString("mv.all_app_vol_periods_purpose_p").split(",").toSeq
  val CRED_SCORE_BY_LOAN_PERIODS_PURPOSE_PURCHASE: Seq[String] = config.getString("mv.app_vol_periods_purpose_p_suffix").split(",").toSeq
  val CRED_SCORE_BY_RE_PERIODS_PURPOSE_PURCHASE: Seq[String] = config.getString("mv.app_vol_periods_purpose_p_suffix").split(",").toSeq
  val CLTV_BY_LOAN_PERIODS_PURPOSE_PURCHASE: Seq[String] = config.getString("mv.app_vol_periods_purpose_p_suffix").split(",").toSeq
  val CLTV_BY_RE_PERIODS_PURPOSE_PURCHASE: Seq[String] = config.getString("mv.app_vol_periods_purpose_p_suffix").split(",").toSeq
  val DTI_BY_LOAN_PERIODS_PURPOSE_PURCHASE: Seq[String] = config.getString("mv.app_vol_periods_purpose_p_suffix").split(",").toSeq
  val DTI_BY_RE_PERIODS_PURPOSE_PURCHASE: Seq[String] = config.getString("mv.app_vol_periods_purpose_p_suffix").split(",").toSeq
  val DENIAL_RATES_BY_LOAN_PERIODS_PURPOSE_PURCHASE: Seq[String] = config.getString("mv.app_vol_periods_purpose_p_suffix").split(",").toSeq
  val DENIAL_RATES_BY_RE_PERIODS_PURPOSE_PURCHASE: Seq[String] = config.getString("mv.app_vol_periods_purpose_p_suffix").split(",").toSeq
  val INTEREST_RATES_BY_LOAN_PERIODS_PURPOSE_PURCHASE: Seq[String] = config.getString("mv.app_vol_periods_purpose_p_suffix").split(",").toSeq
  val INTEREST_RATES_BY_RE_PERIODS_PURPOSE_PURCHASE: Seq[String] = config.getString("mv.app_vol_periods_purpose_p_suffix").split(",").toSeq
  val TLC_BY_LOAN_PERIODS_PURPOSE_PURCHASE: Seq[String] = config.getString("mv.app_vol_periods_purpose_p_suffix").split(",").toSeq
  val TLC_BY_RE_PERIODS_PURPOSE_PURCHASE: Seq[String] = config.getString("mv.app_vol_periods_purpose_p_suffix").split(",").toSeq
  //loan purpose refinance
  val APP_VOL_PERIODS_PURPOSE_REFINANCE: Seq[String] = config.getString("mv.app_vol_periods_purpose_r").split(",").toSeq
  val ALL_APP_VOL_PERIODS_PURPOSE_REFINANCE: Seq[String] = config.getString("mv.all_app_vol_periods_purpose_r").split(",").toSeq
  val CRED_SCORE_BY_LOAN_PERIODS_PURPOSE_REFINANCE: Seq[String] = config.getString("mv.cred_score_by_loan_periods_purpose_r").split(",").toSeq
  val CRED_SCORE_BY_RE_PERIODS_PURPOSE_REFINANCE: Seq[String] = config.getString("mv.cred_score_by_re_periods_purpose_r").split(",").toSeq
  val CLTV_BY_LOAN_PERIODS_PURPOSE_REFINANCE: Seq[String] = config.getString("mv.cltv_by_loan_periods_purpose_r").split(",").toSeq
  val CLTV_BY_RE_PERIODS_PURPOSE_REFINANCE: Seq[String] = config.getString("mv.cltv_by_re_periods_purpose_r").split(",").toSeq
  val DTI_BY_LOAN_PERIODS_PURPOSE_REFINANCE: Seq[String] = config.getString("mv.dti_by_loan_periods_purpose_r").split(",").toSeq
  val DTI_BY_RE_PERIODS_PURPOSE_REFINANCE: Seq[String] = config.getString("mv.dti_by_re_periods_purpose_r").split(",").toSeq
  val DENIAL_RATES_BY_LOAN_PERIODS_PURPOSE_REFINANCE: Seq[String] = config.getString("mv.denial_rates_by_loan_periods_purpose_r").split(",").toSeq
  val DENIAL_RATES_BY_RE_PERIODS_PURPOSE_REFINANCE: Seq[String] = config.getString("mv.denial_rates_by_re_periods_purpose_r").split(",").toSeq
  val INTEREST_RATES_BY_LOAN_PERIODS_PURPOSE_REFINANCE: Seq[String] = config.getString("mv.interest_rates_by_loan_periods_purpose_r").split(",").toSeq
  val INTEREST_RATES_BY_RE_PERIODS_PURPOSE_REFINANCE: Seq[String] = config.getString("mv.interest_rates_by_re_periods_purpose_r").split(",").toSeq
  val TLC_BY_LOAN_PERIODS_PURPOSE_REFINANCE: Seq[String] = config.getString("mv.tlc_by_loan_periods_purpose_r").split(",").toSeq
  val TLC_BY_RE_PERIODS_PURPOSE_REFINANCE: Seq[String] = config.getString("mv.tlc_by_re_periods_purpose_r").split(",").toSeq
}