package hmda.api.http

import hmda.api.model._
import hmda.validation.engine._

trait ValidationErrorConverter {

  def validationErrorsToEditResults(tsErrors: Seq[ValidationError], larErrors: Seq[ValidationError], validationErrorType: ValidationErrorType) = {
    val errorsByType: Map[ValidationErrorType, Seq[ValidationError]] = larErrors.groupBy(_.errorType)

    val editValues: Map[ValidationErrorType, Map[String, Seq[ValidationError]]] =
      errorsByType.mapValues(x => x.groupBy(_.name))

    val tsNamedErrors: Seq[String] = tsErrors.map(_.name)
    val tsUniqueErrors: Seq[String] = tsNamedErrors.diff(larErrors.map(_.name))
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

  def validationErrorsToMacroResults(errors: Seq[ValidationError]): MacroResults = {
    val macroValidationErrors: Seq[ValidationError] = errors.filter(_.errorType == Macro).asInstanceOf[Seq[MacroValidationError]]
    val macroEditNames = macroValidationErrors.map(x => x.name)
    val macroJustifications = MacroEditJustificationLookup().justifications
      .filter(x => macroEditNames.contains(x.edit))
      .map(x => x.justification)
    MacroResults(macroValidationErrors.map(x => MacroResult(x.name, MacroEditJustificationLookup.updateJustifications(x.name, x.asInstanceOf[MacroValidationError].justifications))))
  }

}
