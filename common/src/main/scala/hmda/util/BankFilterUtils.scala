package hmda.util

import com.typesafe.config.ConfigFactory
import hmda.model.filing.submission.SubmissionId
import org.slf4j.LoggerFactory
import hmda.utils._

object BankFilterUtils {
  val log = LoggerFactory.getLogger("hmda")
  val config  = ConfigFactory.load()

  def getFilterList(): Array[String] = {
    val bankFilterConfig = config.getConfig("filter")
    bankFilterConfig.getString("bank-filter-list").toUpperCase.split(",")
  }

  def filterBankWithLogging(institutionLei: String): Boolean = {
    if (getFilterList().contains(institutionLei.toUpperCase)) {
      log.info(s"Filtered out $institutionLei.toUpperCase()")
      false
    } else true
  }


  def filterQuarterlyFilingAlt(submissionId: SubmissionId): Boolean = {
    if (YearUtils.isQuarterlyFiling(submissionId)) {
      println ("failed")
      log.info(s"Filtered out $submissionId.toString().toUpperCase()")
      false
    } else true
  }

  def filterQuarterlyFiling(submissionId: SubmissionId): Boolean =
    YearUtils.isQuarterlyFiling(submissionId)
}
