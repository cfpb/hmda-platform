package hmda.util

import hmda.model.filing.submission.SubmissionId
import hmda.publication.KafkaUtils.config
import org.slf4j.LoggerFactory
import hmda.utils._

object BankFilterUtils {
  val log = LoggerFactory.getLogger("hmda")

  def filterBankWithLogging(institutionLei: String): Boolean = {
  val bankFilterConfig = config.getConfig("filter")
  val bankFilterList =
    bankFilterConfig.getString("bank-filter-list").toUpperCase.split(",")

    if (bankFilterList.contains(institutionLei.toUpperCase)) {
      log.info(s"Filtered out $institutionLei.toUpperCase")
      false
    } else true
  }

  def filterQuarterlyFiling(submissionId: SubmissionId): Boolean =
    YearUtils.isQuarterlyFiling(submissionId)
}
