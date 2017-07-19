package hmda.persistence.demo

import hmda.model.ResourceUtils
import hmda.model.fi._

object DemoFilings extends ResourceUtils {

  val values: Seq[Filing] = {
    val lines = resourceLines("/demoFilings.csv")

    lines.map { line =>
      val values = line.split('|').map(_.trim)
      val period = values(0)
      val institutionId = values(1)
      val filingRequired = values(2).toBoolean
      val start = values(3).toLong
      val end = values(4).toLong

      Filing(
        period,
        institutionId,
        NotStarted,
        filingRequired,
        start,
        end
      )
    }.toSeq
  }

}
