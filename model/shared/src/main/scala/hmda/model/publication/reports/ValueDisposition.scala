package hmda.model.publication.reports

case class ValueDisposition(
    dispositionName: String,
    count: Int,
    value: Int
) {
  def +(disp: ValueDisposition): ValueDisposition = {
    ValueDisposition(dispositionName, count + disp.count, value + disp.value)
  }

  def toJsonFormat: String = {
    s"""
       |
       |{
       |  "disposition": "$dispositionName",
       |  "count": $count,
       |  "value": $value
       |}
       |
        """
  }
}
