package hmda.api.util

import hmda.api.model.{ EditResult, EditResults, LarEditResult }
import hmda.validation.engine.{ ValidationError, ValidationErrorType }

trait EditResultConverter {
  def validationErrorsToEditResults(errors: Seq[ValidationError]): EditResults = {
    val errorsByType: Map[ValidationErrorType, Seq[ValidationError]] = errors.groupBy(_.errorType)
    val editValues: Map[ValidationErrorType, Map[String, Seq[ValidationError]]] =
      errorsByType.mapValues(x => x.groupBy(_.name))

    val larEditResults: Map[ValidationErrorType, Map[String, Seq[LarEditResult]]] =
      editValues.mapValues(x => x.mapValues(y => y.map(_.errorId).map(x => LarEditResult(x))))

    val editResultSeq: Seq[EditResult] = larEditResults.toSeq.map { x =>
      EditResult(x._2.keys.head, x._2.values.flatten.toSeq)
    }

    EditResults(editResultSeq)

  }
}
