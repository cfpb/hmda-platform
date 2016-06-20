package hmda.api.protocol.processing

import hmda.model.fi._
import spray.json.{ DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat }

trait SubmissionProtocol extends DefaultJsonProtocol {

  implicit object SubmissionStatusJsonFormat extends RootJsonFormat[SubmissionStatus] {
    override def write(status: SubmissionStatus): JsValue = {
      status match {
        case Created => JsString("created")
        case Uploading => JsString("uploading")
        case Uploaded => JsString("uploaded")
        case Parsing => JsString("parsing")
        case Parsed => JsString("parsed")
        case ValidatingSyntaxAndValidity => JsString("validating syntactical and validity")
        case ValidatedSyntaxAndValidity => JsString("validated syntactical and validity")
        case ValidatingQualityAndMacro => JsString("validating quality and macro")
        case Unverified => JsString("unverified")
        case Verified => JsString("verified")
        case Signed => JsString("signed")
        case Failed(msg) => JsString(s"failed: $msg")

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
        case _ => throw new DeserializationException("Submission Status expected")

      }
    }
  }

  implicit val submissionFormat = jsonFormat2(Submission.apply)

}
