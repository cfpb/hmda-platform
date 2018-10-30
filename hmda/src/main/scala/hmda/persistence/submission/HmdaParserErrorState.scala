package hmda.persistence.submission

case class HmdaParserErrorState(linesWithErrorCount: Int = 0) {
  def incrementErrorCount: HmdaParserErrorState = {
    HmdaParserErrorState(this.linesWithErrorCount + 1)
  }
}
