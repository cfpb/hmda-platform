package hmda.quarterly.data.api.route


object TitleSuffixAppender {

  def addSuffix(endpoint: String): String = {
    endpoint match {
      case string: String if (string.contains("home")) => " - Home Purchase"
      case string: String if (string.contains("refinance")) => " - Refinance"
      case _  => ""
    }
  }


}