package hmda.api.http

import akka.actor.ActorRef
import akka.pattern.ask
import hmda.api.model._
import hmda.census.model.CbsaLookup
import hmda.model.edits.EditMetaDataLookup
import hmda.model.fi.{ HmdaFileRow, HmdaRowError }
import hmda.model.validation.ValidationError
import hmda.persistence.processing.HmdaFileValidator.{ GetFieldValues, HmdaFileValidationState }
import spray.json.{ JsObject, JsValue }

import scala.concurrent.Future
import scala.util.{ Success, Failure }

trait ValidationErrorConverter {

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

  def validationErrorToResultRow(err: ValidationError, validator: ActorRef): EditResultRow = {
    val releventFields = releventFields(err, validator)
    EditResultRow(RowId(err.publicErrorId), relevantFields(err, validator))
  }

  //// Helper methods

  private def editDescription(editName: String): String = {
    EditMetaDataLookup.forEdit(editName).editDescription
  }

  private def relevantFields(err: ValidationError, validator: ActorRef): Future[JsObject] = {
    val fieldNames: Seq[String] = EditMetaDataLookup.forEdit(err.ruleName).fieldNames

    val mapping = for {
      jsMapping <- (validator ? GetFieldValues(err, fieldNames)).mapTo[Seq[(String, JsValue)]]
    } yield jsMapping

    mapping.onComplete({
      case Success(Seq[(String, )]) =>
    })
  }
}
