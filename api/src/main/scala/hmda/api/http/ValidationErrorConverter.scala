package hmda.api.http

import hmda.api.model._
import hmda.validation.engine._

trait ValidationErrorConverter {

  def validationErrorsToEditResults(tsErrors: Seq[ValidationError], larErrors: Seq[ValidationError], validationErrorType: ValidationErrorType): EditResults = {

    def singleTypeValidationErrorsToEditResults(errors: Seq[ValidationError], validationErrorType: ValidationErrorType): Seq[EditResult] = {
      val errorsByType: Map[ValidationErrorType, Seq[ValidationError]] = errors.groupBy(_.errorType)

      val editValues: Map[ValidationErrorType, Map[ValidationErrorMetaData, Seq[ValidationError]]] =
        errorsByType.mapValues(x => x.groupBy(y => y.metaData))

      val larEditResults: Map[ValidationErrorType, Map[ValidationErrorMetaData, Seq[LarEditResult]]] =
        editValues.mapValues(x => x.mapValues(y => y.map(z => LarEditResult(z.errorId, z.metaData.fields.map(a => LarEditField(a._1.name, a._2)).toList))))

      val mapResults = larEditResults.getOrElse(validationErrorType, Map.empty[ValidationErrorMetaData, Seq[LarEditResult]])
      mapResults
        .toList
        .map(x => EditResult(x._1.name, x._1.description, x._1.fields.toList.map(y => y._1), x._2))
    }

    val tsErrorsRenamed: Seq[ValidationError] = {
      tsErrors.map(x => {
        x.copy(errorId = "Transmittal Sheet")
      })
    }

    val tsEdits = singleTypeValidationErrorsToEditResults(tsErrorsRenamed, validationErrorType)
    val larEdits = singleTypeValidationErrorsToEditResults(larErrors, validationErrorType)
    val larEditNames = larEdits.map(_.edit)

    val tsUnique = tsEdits.filter(x => !larEditNames.contains(x.edit))
    val tsDup = tsEdits.diff(tsUnique)

    EditResults(larEdits.map(x => {
      val ts = tsDup.filter(y => x.edit == y.edit)
      if (ts.length == 1) x.copy(lars = x.lars ++ ts.head.lars)
      else x
    }) ++ tsUnique)

  }

  def validationErrorsToMacroResults(errors: Seq[ValidationError]): MacroResults = {
    val macroValidationErrors: Seq[ValidationError] = errors.filter(_.errorType == Macro)
    MacroResults(macroValidationErrors.map(x => MacroResult(x.metaData.name, List())))
  }
}
