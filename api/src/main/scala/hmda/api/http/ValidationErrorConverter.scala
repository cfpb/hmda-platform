package hmda.api.http

import hmda.api.model.{ EditResult, EditResults, LarEditResult, LarId }
import hmda.validation.engine._

trait ValidationErrorConverter {

  def validationErrorsToEditResults(tsErrors: Seq[ValidationError], larErrors: Seq[ValidationError], validationErrorType: ValidationErrorType) = {
    val errorsByType: Map[ValidationErrorType, Seq[ValidationError]] = larErrors.groupBy(_.errorType)

    val editValues: Map[ValidationErrorType, Map[String, Seq[ValidationError]]] =
      errorsByType.mapValues(x => x.groupBy(_.name))

    val tsNamedErrors: Seq[String] = tsErrors.map(_.errorId)
    val tsUniqueErrors: Seq[String] = tsNamedErrors.diff(larErrors.map(_.errorId))
    val tsEditResults: Seq[EditResult] = tsUniqueErrors.map(x => EditResult(x, ts = true, Nil))

    val larEditResults: Map[ValidationErrorType, Map[String, Seq[LarEditResult]]] =
      editValues.mapValues(x => x.mapValues(y => y.map(_.errorId).map(z => LarEditResult(LarId(z)))))

    val mapResults = larEditResults.getOrElse(validationErrorType, Map.empty[String, Seq[LarEditResult]])
    EditResults(
      mapResults
        .toList
        .map(x => EditResult(x._1, tsNamedErrors.contains(x._1), x._2))
        .union(tsEditResults)
    )

  }

}
