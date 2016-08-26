package hmda.api.protocol.processing

import hmda.model.fi._
import hmda.api.model.{ SubmissionStatusWrapper, SubmissionWrapper, Submissions }
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat }

trait SubmissionProtocol extends DefaultJsonProtocol {

  implicit object SubmissionStatusJsonFormat extends RootJsonFormat[SubmissionStatus] {
    override def write(status: SubmissionStatus): JsValue = {
      status match {
        case Failed(msg) => JsString(s"failed: $msg")
        case _ => JsString(status.message)
      }
    }

    override def read(json: JsValue): SubmissionStatus = {
      json match {
        case JsString(s) => s match {
          case "created" => Created
          case "uploading" => Uploading
          case "uploaded" => Uploaded
          case "parsing" => Parsing
          case "parsed" => Parsed
          case "validating syntactical and validity" => ValidatingSyntaxAndValidity
          case "validated syntactical and validity" => ValidatedSyntaxAndValidity
          case "validating quality and macro" => ValidatingQualityAndMacro
          case "unverified" => Unverified
          case "verified" => Verified
          case "signed" => Signed
          case "failed" => Failed("")
          case _ => throw new DeserializationException("Submission Status expected")
        }
        case _ => throw new DeserializationException("Unable to deserialize")

      }
    }
  }

  implicit val submissionFormat = jsonFormat2(Submission.apply)
  implicit val submissionsFormat = jsonFormat1(Submissions.apply)
  implicit val submissionStatusWrapperFormat = jsonFormat2(SubmissionStatusWrapper.apply)
  implicit val submissionWrapperFormat = jsonFormat2(SubmissionWrapper.apply)

}
