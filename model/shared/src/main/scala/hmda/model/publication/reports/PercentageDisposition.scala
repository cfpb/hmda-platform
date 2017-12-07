package hmda.model.publication.reports

case class PercentageDisposition(
    dispositionName: String,
    count: Int,
    percentage: Int
) {

  def toJsonFormat: String = {
    s"""
       |
       |{
       |  "disposition": "$dispositionName",
       |  "count": $count,
       |  "percentage": $percentage
       |}
       |
        """
  }
}

object PercentageDisposition {

  def collectionJson(dispositions: List[PercentageDisposition]): String = {
    dispositions.map(disp => disp.toJsonFormat).mkString("[", ",", "]")
  }
}
