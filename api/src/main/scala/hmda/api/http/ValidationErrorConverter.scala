package hmda.api.http

import hmda.api.model._
import hmda.model.edits.EditMetaDataLookup
import hmda.validation.engine._

trait ValidationErrorConverter {

  val editDescriptions = EditMetaDataLookup.values

  def validationErrorsToEditResults(tsErrors: Seq[ValidationError], larErrors: Seq[ValidationError], validationErrorType: ValidationErrorType): EditResults = {

    val tsEdits = singleTypeValidationErrorsToEditResults(tsErrors, validationErrorType)
    val larEdits = singleTypeValidationErrorsToEditResults(larErrors, validationErrorType)
    val larEditNames = larEdits.map(_.edit)

    val tsPartition = tsEdits.partition(x => !larEditNames.contains(x.edit))
    val tsUnique = tsPartition._1
    val tsDup = tsPartition._2

    val tsUniqueRenamed = tsUnique.map(x => x.copy(lars = x.lars.map(y => y.copy(lar = LarId("Transmittal Sheet")))))

    EditResults(larEdits.map(x => {
      val tsOption = tsDup.find(y => x.edit == y.edit)
      tsOption match {
        case Some(_) => x.copy(lars = x.lars :+ LarEditResult(LarId("Transmittal Sheet")))
        case None => x
      }
    }) ++ tsUniqueRenamed)

  }

  private def singleTypeValidationErrorsToEditResults(errors: Seq[ValidationError], validationErrorType: ValidationErrorType): Seq[EditResult] = {
    val errorsByType: Map[ValidationErrorType, Seq[ValidationError]] = errors.groupBy(_.errorType)

    val editValues: Map[ValidationErrorType, Map[String, Seq[ValidationError]]] =
      errorsByType.mapValues(x => x.groupBy(y => y.ruleName))

    val larEditResults: Map[ValidationErrorType, Map[String, Seq[LarEditResult]]] =
      editValues.mapValues(x => x.mapValues(y => y.map(z => LarEditResult(LarId(z.errorId)))))

    val mapResults = larEditResults.getOrElse(validationErrorType, Map.empty[String, Seq[LarEditResult]])

    mapResults
      .toList
      .map(x => EditResult(x._1, findEditDescription(x._1), x._2))

  }

  private def findEditDescription(editName: String): String = {
    editDescriptions.find(x => x.editNumber == editName)
      .map(_.editDescription)
      .getOrElse("")
  }

  def validationErrorsToMacroResults(errors: Seq[ValidationError]): MacroResults = {
    val macroValidationErrors: Seq[MacroValidationError] = errors.filter(_.errorType == Macro).asInstanceOf[Seq[MacroValidationError]]
    MacroResults(macroValidationErrors.map(x => MacroResult(x.ruleName, MacroEditJustificationLookup.updateJustifications(x.ruleName, x.justifications))))
  }

}
