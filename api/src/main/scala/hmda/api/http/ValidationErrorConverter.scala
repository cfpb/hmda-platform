package hmda.api.http

import hmda.api.model._
import hmda.model.edits.{ EditMetaData, EditMetaDataLookup }
import hmda.validation.engine._

trait ValidationErrorConverter {

  val editDescriptions = EditMetaDataLookup.values

  def validationErrorsToEditResults(tsErrors: Seq[ValidationError], larErrors: Seq[ValidationError], validationErrorType: ValidationErrorType) = {

    val errorsByType: Map[ValidationErrorType, Seq[ValidationError]] = larErrors.groupBy(_.errorType)

    val editValues: Map[ValidationErrorType, Map[String, Seq[ValidationError]]] =
      errorsByType.mapValues(x => x.groupBy(y => y.ruleName))

    val tsNamedErrors: Seq[String] = tsErrors.map(_.ruleName)
    val tsUniqueErrors: Seq[String] = tsNamedErrors.diff(larErrors.map(_.ruleName))
    val tsEditResults: Seq[EditResult] = tsUniqueErrors.map(x => EditResult(x, findEditDescription(x), findEditFields(x), ts = true, Nil))

    val larEditResults: Map[ValidationErrorType, Map[String, Seq[LarEditResult]]] =
      editValues.mapValues(x => x.mapValues(y => y.map(_.errorId).map(z => LarEditResult(LarId(z)))))

    val mapResults = larEditResults.getOrElse(validationErrorType, Map.empty[String, Seq[LarEditResult]])
    EditResults(
      mapResults
        .toList
        .map(x => EditResult(x._1, findEditDescription(x._1), findEditFields(x._1), tsNamedErrors.contains(x._1), x._2))
        .union(tsEditResults)
    )

  }

  private def findEditDescription(editName: String): String = {
    editDescriptions.find(x => x.editNumber == editName)
      .map(_.editDescription)
      .getOrElse("")
  }

  private def findEditFields(editName: String): List[String] = {
    editDescriptions.find(x => x.editNumber == editName)
      .map(_.fieldNames.split(",").toList)
      .getOrElse(List(""))
  }

  def validationErrorsToMacroResults(errors: Seq[ValidationError]): MacroResults = {
    val macroValidationErrors: Seq[ValidationError] = errors.filter(_.errorType == Macro)
    MacroResults(macroValidationErrors.map(x => MacroResult(x.ruleName, List())))
  }
}
