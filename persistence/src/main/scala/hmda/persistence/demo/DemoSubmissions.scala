package hmda.persistence.demo

import hmda.model.ResourceUtils
import hmda.model.fi._

object DemoSubmissions extends ResourceUtils {

  val values: Seq[(String, String, Submission)] = {
    val lines = resourceLines("/demoSubmissions.csv")

    lines.map { line =>
      val values = line.split('|').map(_.trim)
      val institutionId = values(0)
      val period = values(1)
      val number = values(2).toInt
      val status = toSubmissionStatus(values(3))
      val id = SubmissionId(institutionId, period, number)
      val start = values(4).toLong
      val end = values(5).toLong

      (
        institutionId,
        period,
        Submission(
          id,
          status,
          start,
          end
        )
      )
    }.toSeq
  }

  def toSubmissionStatus(text: String): SubmissionStatus = {
    text match {
      case "created" => Created
      case "uploading" => Uploading
      case "uploaded" => Uploaded
      case "parsing" => Parsing
      case "parsed-with-errors" => ParsedWithErrors
      case "parsed" => Parsed
      case "validating" => Validating
      case "validated-with-errors" => ValidatedWithErrors
      case "validated" => Validated
      case "signed" => Signed
      case "failed" => Failed("")
    }
  }

}
