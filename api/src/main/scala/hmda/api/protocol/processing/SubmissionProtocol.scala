package hmda.api.protocol.processing

import hmda.api.model.institutions.submissions.{ ContactSummary, FileSummary, RespondentSummary, SubmissionSummary }
import hmda.model.fi._
import hmda.api.model.{ Receipt, Submissions }
import hmda.api.protocol.validation.ValidationResultProtocol
import spray.json.{ DeserializationException, JsNumber, JsObject, JsString, JsValue, RootJsonFormat }

trait SubmissionProtocol extends ValidationResultProtocol {

  implicit object SubmissionStatusJsonFormat extends RootJsonFormat[SubmissionStatus] {
    override def write(status: SubmissionStatus): JsValue = {
      JsObject(
        "code" -> JsNumber(status.code),
        "message" -> JsString(status.message),
        "description" -> JsString(status.description)
      )
    }

    override def read(json: JsValue): SubmissionStatus = {
      json.asJsObject.getFields("code").head match {
        case JsNumber(s) => s.toInt match {
          case 1 => Created
          case 2 => Uploading
          case 3 => Uploaded
          case 4 => Parsing
          case 5 => ParsedWithErrors
          case 6 => Parsed
          case 7 => Validating
          case 8 => ValidatedWithErrors
          case 9 => Validated
          case 10 => Signed
          case -1 =>
            val message = json.asJsObject.getFields("message").head.toString()
            Failed(message.substring(1, message.length - 1))
          case _ => throw DeserializationException("Submission Status expected")
        }
        case _ => throw DeserializationException("Unable to deserialize")

      }
    }
  }

  implicit val submissionIdProtocol = jsonFormat3(SubmissionId.apply)
  implicit val submissionFormat = jsonFormat5(Submission.apply)
  implicit val submissionsFormat = jsonFormat1(Submissions.apply)
  implicit val receiptFormat = jsonFormat3(Receipt.apply)

  implicit val fileSummaryFormat = jsonFormat3(FileSummary.apply)
  implicit val contactSummaryFormat = jsonFormat3(ContactSummary.apply)
  implicit val respondentSummaryFormat = jsonFormat5(RespondentSummary.apply)
  implicit val submissionSummaryFormat = jsonFormat2(SubmissionSummary.apply)
}
