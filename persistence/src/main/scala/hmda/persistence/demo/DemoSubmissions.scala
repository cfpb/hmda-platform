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
      val seqNo = values(2).toInt
      val id = SubmissionId(institutionId, period, seqNo)
      val status = toSubmissionStatus(values(3))

      (
        institutionId,
        period,
        Submission(
          id,
          status
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
      case "parsed" => Parsed
      case "validating-syntax-and-validity" => ValidatingSyntaxAndValidity
      case "validated-syntax-and-validity" => ValidatedSyntaxAndValidity
      case "validating-quality-and-macro" => ValidatingQualityAndMacro
      case "unverified" => Unverified
      case "verified" => Verified
      case "signed" => Signed
      case "failed" => Failed("")
    }
  }

}
