package hmda.model.processing.state

import hmda.model.filing.submission

object ValidationProgressTrackerState {
  def initialize(s: HmdaValidationErrorState): ValidationProgressTrackerState = {
    val syntacticalValidation =
      if (s.statusCode == submission.SyntacticalOrValidityErrors.code) ValidationProgress.CompletedWithErrors
      else if (s.statusCode == submission.SyntacticalOrValidity.code) ValidationProgress.Completed
      else ValidationProgress.Waiting

    val qualityValidation =
      if (s.statusCode == submission.QualityErrors.code) ValidationProgress.CompletedWithErrors
      else if (s.statusCode == submission.Quality.code) ValidationProgress.Completed
      else ValidationProgress.Waiting

    val macroValidation =
      if (s.statusCode == submission.MacroErrors.code) ValidationProgress.CompletedWithErrors
      else if (s.statusCode == submission.Verified.code) ValidationProgress.Completed
      else ValidationProgress.Waiting

    ValidationProgressTrackerState(syntacticalValidation, qualityValidation, macroValidation)
  }
}

case class ValidationProgressTrackerState(
                                           syntacticalValidation: ValidationProgress,
                                           qualityValidation: ValidationProgress,
                                           macroValidation: ValidationProgress
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
      syntacticalValidation,
      qualityValidation,
      macroValidation
    )
  }

  private def adjustProgress(incoming: ValidationProgress, existing: ValidationProgress): ValidationProgress =
    existing match {
      case ValidationProgress.Waiting             => incoming
      case ValidationProgress.InProgress(_)       => incoming
      case ValidationProgress.Completed           => ValidationProgress.Completed
      case ValidationProgress.CompletedWithErrors => ValidationProgress.CompletedWithErrors
    }

  def updateQuality(progress: ValidationProgress): ValidationProgressTrackerState = {
    val adjustedProgress = adjustProgress(incoming = progress, existing = self.qualityValidation)
    self.copy(qualityValidation = adjustedProgress)
  }

  def updateSyntactical(progress: ValidationProgress): ValidationProgressTrackerState = {
    val adjustedProgress = adjustProgress(incoming = progress, self.syntacticalValidation)
    self.copy(syntacticalValidation = adjustedProgress)
  }

  def updateMacro(progress: ValidationProgress): ValidationProgressTrackerState = {
    val adjustedProgress = adjustProgress(incoming = progress, self.macroValidation)
    self.copy(macroValidation = adjustedProgress)
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