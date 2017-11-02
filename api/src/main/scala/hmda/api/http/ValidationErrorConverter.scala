package hmda.api.http

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.model._
import hmda.model.edits.EditMetaDataLookup
import hmda.model.validation.ValidationError
import hmda.persistence.processing.HmdaFileValidator.{ GetFieldValues, HmdaFileValidationState }
import spray.json.JsObject

import scala.concurrent.{ ExecutionContext, Future }

trait ValidationErrorConverter {
  implicit val timeout: Timeout

  def editsOfType(errType: String, vs: HmdaFileValidationState): Seq[ValidationError] = {
    errType.toLowerCase match {
      case "syntactical" => vs.syntacticalErrors
      case "validity" => vs.validityErrors
      case "quality" => vs.qualityErrors
      case "macro" => vs.larMacro
      case _ => Seq()
    }
  }

  def editInfos(edits: Seq[ValidationError]): Seq[EditInfo] = {
    val errsByEdit: Map[String, Seq[ValidationError]] = edits.groupBy(_.ruleName)

    errsByEdit.map {
      case (editName: String, _) =>
        EditInfo(editName, editDescription(editName))
    }.toSeq
  }

  def validationErrorsToCsvResults(vs: HmdaFileValidationState): String = {
    val errors: Seq[ValidationError] = vs.allErrors
    val rows: Seq[String] = errors.map(_.toCsv)
    "editType, editId, loanId\n" + rows.mkString("\n")
  }

  def validationErrorToResultRow(err: ValidationError, validator: ActorRef)(implicit ec: ExecutionContext): Future[EditResultRow] = {
    for {
      fields <- relevantFields(err, validator)
    } yield EditResultRow(RowId(err.publicErrorId), fields)
  }

  //// Helper methods

  private def editDescription(editName: String): String = {
    EditMetaDataLookup.forEdit(editName).editDescription
  }

  private def relevantFields(err: ValidationError, validator: ActorRef)(implicit ec: ExecutionContext): Future[JsObject] = {
    val fieldNames: Seq[String] = EditMetaDataLookup.forEdit(err.ruleName).fieldNames

    for {
      jsMapping <- (validator ? GetFieldValues(err, fieldNames)).mapTo[JsObject]
    } yield jsMapping
  }
}
