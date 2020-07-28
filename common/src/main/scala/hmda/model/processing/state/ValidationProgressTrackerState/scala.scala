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

    ValidationProgressTrackerState(
      syntacticalValidation,
      qualityValidation,
      macroValidation,
      macroVerified = s.macroVerified,
      qualityVerified = s.macroVerified,
      submissionSigned = s.statusCode == submission.Signed.code,
      totalLinesInFile = Long.MaxValue
    )
  }
}

case class ValidationProgressTrackerState(
                                           syntacticalValidation: ValidationProgress,
                                           qualityValidation: ValidationProgress,
                                           macroValidation: ValidationProgress,
                                           macroVerified: Boolean,
                                           qualityVerified: Boolean,
                                           submissionSigned: Boolean,
                                           totalLinesInFile: Long
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

    val submissionSigned =
      s.statusCode == submission.Signed.code

    self.copy(
      syntacticalValidation,
      qualityValidation,
      macroValidation,
      s.macroVerified,
      s.qualityVerified,
      submissionSigned
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

  def updateLines(incoming: Long): ValidationProgressTrackerState =
    self.copy(totalLinesInFile = incoming)
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
  case object Waiting                       extends ValidationProgress
  case class InProgress(linesFinished: Int) extends ValidationProgress
  case object Completed                     extends ValidationProgress
  case object CompletedWithErrors           extends ValidationProgress
}

sealed trait VerificationType
object VerificationType {
  case object Quality extends VerificationType
  case object Macro   extends VerificationType
}