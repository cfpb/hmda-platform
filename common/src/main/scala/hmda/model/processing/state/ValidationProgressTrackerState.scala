package hmda.model.processing.state

import hmda.model.filing.submission

object ValidationProgressTrackerState {
  def initialize(s: HmdaValidationErrorState): ValidationProgressTrackerState = {
    ValidationProgressTrackerState(ValidationProgress.Waiting, Set(), ValidationProgress.Waiting, Set(), ValidationProgress.Waiting, Set())
      .fromSnapshot(s)
  }
}

case class ValidationProgressTrackerState(
                                           syntacticalValidation: ValidationProgress,
                                           syntacticalEdits: Set[String],
                                           qualityValidation: ValidationProgress,
                                           qualityEdits: Set[String],
                                           macroValidation: ValidationProgress,
                                           macroEdits: Set[String]
                                         ) { self =>
  def fromSnapshot(s: HmdaValidationErrorState): ValidationProgressTrackerState = {
    val syntacticalValidation =
      if (s.statusCode == submission.SyntacticalOrValidityErrors.code) ValidationProgress.CompletedWithErrors
      else if (s.statusCode == submission.SyntacticalOrValidity.code) ValidationProgress.Completed
      else self.syntacticalValidation

    val qualityValidation =
      if (s.statusCode == submission.QualityErrors.code) ValidationProgress.CompletedWithErrors
      else if (s.statusCode == submission.Quality.code) ValidationProgress.Completed
      else self.qualityValidation

    val macroValidation =
      if (s.statusCode == submission.MacroErrors.code) ValidationProgress.CompletedWithErrors
      else if (s.statusCode == submission.Verified.code) ValidationProgress.Completed
      else self.macroValidation

    self.copy(
      syntacticalValidation, s.syntactical.map(_.editName),
      qualityValidation, s.quality.map(_.editName),
      macroValidation, s.`macro`.map(_.editName)
    )
  }

  private def adjustProgress(incoming: ValidationProgress, existing: ValidationProgress): ValidationProgress =
    existing match {
      case ValidationProgress.Waiting             => incoming
      case ValidationProgress.InProgress(_)       => incoming
      case ValidationProgress.Completed           => ValidationProgress.Completed
      case ValidationProgress.CompletedWithErrors => ValidationProgress.CompletedWithErrors
    }

  def updateQuality(progress: ValidationProgress, editNames: Set[String]): ValidationProgressTrackerState = {
    val adjustedProgress = adjustProgress(incoming = progress, existing = self.qualityValidation)
    self.copy(qualityValidation = adjustedProgress, qualityEdits = qualityEdits ++ editNames)
  }

  def updateSyntactical(progress: ValidationProgress, editNames: Set[String]): ValidationProgressTrackerState = {
    val adjustedProgress = adjustProgress(incoming = progress, self.syntacticalValidation)
    self.copy(syntacticalValidation = adjustedProgress, syntacticalEdits = syntacticalEdits ++ editNames)
  }

  def updateMacro(progress: ValidationProgress, editNames: Set[String]): ValidationProgressTrackerState = {
    val adjustedProgress = adjustProgress(incoming = progress, self.macroValidation)
    self.copy(macroValidation = adjustedProgress, macroEdits = macroEdits ++ editNames)
  }
}

sealed trait ValidationType
object ValidationType {
  case object Syntactical extends ValidationType
  type Syntactical = Syntactical.type

  case object Quality extends ValidationType
  type Quality = Quality.type

  case object Macro extends ValidationType
  type Macro = Macro.type
}

sealed trait ValidationProgress
object ValidationProgress {
  case object Waiting                    extends ValidationProgress
  case class InProgress(percentage: Int) extends ValidationProgress
  case object Completed                  extends ValidationProgress
  case object CompletedWithErrors        extends ValidationProgress

  def progress(v: ValidationProgress): Int =
    v match {
      case Waiting                => 0
      case InProgress(percentage) => percentage
      case Completed              => 100
      case CompletedWithErrors    => 100
    }
}