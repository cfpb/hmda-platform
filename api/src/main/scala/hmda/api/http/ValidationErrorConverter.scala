package hmda.api.http

import hmda.api.model._
import hmda.validation.engine._

trait ValidationErrorConverter {

  def validationErrorsToEditResults(tsErrors: Seq[ValidationError], larErrors: Seq[ValidationError], validationErrorType: ValidationErrorType): EditResults = {

    val tsErrorsRenamed: Seq[ValidationError] = {
      tsErrors.map(x => {
        x.copy(errorId = "Transmittal Sheet")
      })
    }

    val tsEdits = singleTypeValidationErrorsToEditResults(tsErrorsRenamed, validationErrorType)
    val larEdits = singleTypeValidationErrorsToEditResults(larErrors, validationErrorType)
    val larEditNames = larEdits.map(_.edit)

    val tsPartition = tsEdits.partition(x => !larEditNames.contains(x.edit))
    val tsUnique = tsPartition._1
    val tsDup = tsPartition._2

    EditResults(larEdits.map(x => {
      val tsOption = tsDup.find(y => x.edit == y.edit)
      tsOption match {
        case Some(_) => x.copy(lars = x.lars :+ LarEditResult(LarId("Transmittal Sheet")))
        case None => x
      }
    }) ++ tsUnique)

  }

  private def singleTypeValidationErrorsToEditResults(errors: Seq[ValidationError], validationErrorType: ValidationErrorType): Seq[EditResult] = {
    val errorsByType: Map[ValidationErrorType, Seq[ValidationError]] = errors.groupBy(_.errorType)

    val editValues: Map[ValidationErrorType, Map[String, Seq[ValidationError]]] =
      errorsByType.mapValues(x => x.groupBy(y => y.name))

    val larEditResults: Map[ValidationErrorType, Map[String, Seq[LarEditResult]]] =
      editValues.mapValues(x => x.mapValues(y => y.map(z => LarEditResult(LarId(z.errorId)))))

    val mapResults = larEditResults.getOrElse(validationErrorType, Map.empty[String, Seq[LarEditResult]])
    mapResults
      .toList
      .map(x => EditResult(x._1, x._2))
  }

  def validationErrorsToMacroResults(errors: Seq[ValidationError]): MacroResults = {
    val macroValidationErrors: Seq[ValidationError] = errors.filter(_.errorType == Macro)
    MacroResults(macroValidationErrors.map(x => MacroResult(x.name, List())))
  }
}
