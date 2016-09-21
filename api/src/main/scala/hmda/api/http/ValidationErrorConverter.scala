package hmda.api.http

import hmda.api.model.{ EditResult, EditResults, LarEditResult, SummaryEditResults }
import hmda.validation.engine._

trait ValidationErrorConverter {

  def validationErrorsToEditResults(errors: Seq[ValidationError]) = {
    val errorsByType: Map[ValidationErrorType, Seq[ValidationError]] = errors.groupBy(_.errorType)

    val editValues: Map[ValidationErrorType, Map[String, Seq[ValidationError]]] =
      errorsByType.mapValues(x => x.groupBy(_.name))

    val larEditResults: Map[ValidationErrorType, Map[String, Seq[LarEditResult]]] =
      editValues.mapValues(x => x.mapValues(y => y.map(_.errorId).map(z => LarEditResult(z))))

    val s = editResults(larEditResults, Syntactical)
    val v = editResults(larEditResults, Validity)
    val q = editResults(larEditResults, Quality)
    val m = editResults(larEditResults, Macro)

    SummaryEditResults(s, v, q, m)

  }

  private def editResults(larEditResults: Map[ValidationErrorType, Map[String, Seq[LarEditResult]]], validationErrorType: ValidationErrorType): EditResults = {
    val mapResults = larEditResults.getOrElse(validationErrorType, Map.empty[String, Seq[LarEditResult]])
    EditResults(
      mapResults
        .toList
        .map(x => EditResult(x._1, x._2))
    )
  }
}
