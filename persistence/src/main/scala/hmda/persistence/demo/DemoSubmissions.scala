package hmda.persistence.demo

import hmda.model.ResourceUtils

object DemoSubmissions extends ResourceUtils {

  val values: Seq[(String, String)] = {
    val lines = resourceLines("/demoFilings.csv")

    lines.map { line =>
      val values = line.split('|').map(_.trim)
      val institutionId = values(0)
      val period = values(1)

      (
        institutionId,
        period
      )
    }.toSeq
  }

}
