package hmda.persistence.submission

case class HmdaParserErrorState(errorCount: Int = 0) {
  def incrementErrorCount: HmdaParserErrorState = {
    HmdaParserErrorState(this.errorCount + 1)
  }
}
