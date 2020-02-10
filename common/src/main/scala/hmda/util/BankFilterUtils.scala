package hmda.util

import hmda.model.filing.submission.SubmissionId
import hmda.publication.KafkaUtils.config
import org.slf4j.LoggerFactory
import hmda.utils._

object BankFilterUtils {
  val log = LoggerFactory.getLogger("hmda")

  def getFilterList(): Array[String] = {
    val bankFilterConfig = config.getConfig("filter")
    bankFilterConfig.getString("bank-filter-list").toUpperCase.split(",")
  }

  def filterBankWithLogging(institutionLei: String): Boolean = {
    if (getFilterList().contains(institutionLei.toUpperCase)) {
      log.info(s"Filtered out $institutionLei.toUpperCase")
      false
    } else true
  }

  def filterQuarterlyFiling(submissionId: SubmissionId): Boolean =
    YearUtils.isQuarterlyFiling(submissionId)
}
