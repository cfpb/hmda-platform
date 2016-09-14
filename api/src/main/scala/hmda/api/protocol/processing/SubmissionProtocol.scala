package hmda.api.protocol.processing

import hmda.model.fi._
import hmda.model.fi.SubmissionStatusMessage._
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
          case `createdMsg` => Created
          case `uploadingMsg` => Uploading
          case `uploadedMsg` => Uploaded
          case `parsingMsg` => Parsing
          case `parsedMsg` => Parsed
          case `parsedWithErrorsMsg` => ParsedWithErrors
          case `validatingMsg` => Validating
          case `validatedWithErrorsMsg` => ValidatedWithErrors
          case `validatedMsg` => Validated
          case `iRSGeneratedMsg` => IRSGenerated
          case `iRSVerifiedMsg` => IRSVerified
          case `signedMsg` => Signed
          case "failed" => Failed("")
          case _ => throw new DeserializationException("Submission Status expected")
        }
        case _ => throw new DeserializationException("Unable to deserialize")

      }
    }
  }

  implicit val submissionIdProtocol = jsonFormat3(SubmissionId.apply)
  implicit val submissionFormat = jsonFormat2(Submission.apply)
  implicit val submissionsFormat = jsonFormat1(Submissions.apply)
  implicit val submissionStatusWrapperFormat = jsonFormat2(SubmissionStatusWrapper.apply)
  implicit val submissionWrapperFormat = jsonFormat2(SubmissionWrapper.apply)

}
