package hmda.api.http

import hmda.api.model._
import hmda.model.edits.{ EditMetaData, EditMetaDataLookup }
import hmda.validation.engine._

trait ValidationErrorConverter {

  val editDescriptions = EditMetaDataLookup.values

  def validationErrorsToEditResults(tsErrors: Seq[ValidationError], larErrors: Seq[ValidationError], validationErrorType: ValidationErrorType) = {

    def findEditDescription(editName: String): String = {
      editDescriptions.find(x => x.editNumber == editName)
        .map(_.editDescription)
        .getOrElse("")
    }

    def findEditFields(editName: String): List[String] = {
      editDescriptions.find(x => x.editNumber == editName)
        .map(_.fieldNames.split(",").toList)
        .getOrElse(List(""))
    }

    val errorsByType: Map[ValidationErrorType, Seq[ValidationError]] = larErrors.groupBy(_.errorType)

    val editValues: Map[ValidationErrorType, Map[ValidationErrorMetaData, Seq[ValidationError]]] =
      errorsByType.mapValues(x => x.groupBy(y => y.metaData))

    val tsNamedErrors: Seq[ValidationErrorMetaData] = tsErrors.map(_.metaData)
    val tsUniqueErrors: Seq[ValidationErrorMetaData] = tsNamedErrors.diff(larErrors.map(_.metaData))
    val tsEditResults: Seq[EditResult] = tsUniqueErrors.map(x => EditResult(x.name, findEditDescription(x.name), findEditFields(x.name), ts = true, Nil))

    val larEditResults: Map[ValidationErrorType, Map[ValidationErrorMetaData, Seq[LarEditResult]]] =
      editValues.mapValues(x => x.mapValues(y => y.map(_.errorId).map(z => LarEditResult(LarId(z)))))

    val mapResults = larEditResults.getOrElse(validationErrorType, Map.empty[ValidationErrorMetaData, Seq[LarEditResult]])
    EditResults(
      mapResults
        .toList
        .map(x => EditResult(x._1.name, findEditDescription(x._1.name), findEditFields(x._1.name), tsNamedErrors.contains(x._1), x._2))
        .union(tsEditResults)
    )

  }

  def validationErrorsToMacroResults(errors: Seq[ValidationError]): MacroResults = {
    val macroValidationErrors: Seq[ValidationError] = errors.filter(_.errorType == Macro)
    MacroResults(macroValidationErrors.map(x => MacroResult(x.metaData.name, List())))
  }
}
