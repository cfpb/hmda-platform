package hmda.util

import org.slf4j.LoggerFactory

object BankFilterUtils {
  val log = LoggerFactory.getLogger("hmda")

  def filterBankWithLogging(institutionLei: String,
                            bankIgnoreList: Array[String]): Boolean = {
    if (bankIgnoreList.contains(institutionLei.toUpperCase)) {
      log.info(s"Filtered out $institutionLei.toUpperCase")
      false
    } else true
  }
}
