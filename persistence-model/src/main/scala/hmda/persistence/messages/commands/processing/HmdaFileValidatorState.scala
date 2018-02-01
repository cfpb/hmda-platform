package hmda.persistence.messages.commands.processing

import hmda.model.fi.ts.TransmittalSheet
import hmda.model.validation.{ Macro, Quality }
import hmda.persistence.messages.CommonMessages.Event
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.{ LarValidated, TsValidated }
import hmda.persistence.messages.events.processing.HmdaFileValidatorEvents._

object HmdaFileValidatorState {

  case class SVState(
      syntacticalEdits: Set[String] = Set(),
      validityEdits: Set[String] = Set()
  ) {
    def updated(event: Event): SVState = event match {
      case TsSyntacticalError(e) => this.copy(syntacticalEdits = syntacticalEdits + e.ruleName)
      case LarSyntacticalError(e) => this.copy(syntacticalEdits = syntacticalEdits + e.ruleName)
      case TsValidityError(e) => this.copy(validityEdits = validityEdits + e.ruleName)
      case LarValidityError(e) => this.copy(validityEdits = validityEdits + e.ruleName)
    }

    def containsSVEdits = syntacticalEdits.nonEmpty || validityEdits.nonEmpty
  }

  case class QMState(
      qualityEdits: Set[String] = Set(),
      macroEdits: Set[String] = Set()
  ) {
    def updated(event: Event): QMState = event match {
      case TsQualityError(e) => this.copy(qualityEdits = qualityEdits + e.ruleName)
      case LarQualityError(e) => this.copy(qualityEdits = qualityEdits + e.ruleName)
      case LarMacroError(e) => this.copy(macroEdits = macroEdits + e.ruleName)
    }

    def containsQMEdits = qualityEdits.nonEmpty || macroEdits.nonEmpty
  }

  case class HmdaVerificationState(
      qualityVerified: Boolean = false,
      macroVerified: Boolean = false,
      ts: Option[TransmittalSheet] = None,
      larCount: Int = 0
  ) {
    def updated(event: Event): HmdaVerificationState = event match {
      case EditsVerified(editType, v) =>
        if (editType == Quality) this.copy(qualityVerified = v)
        else if (editType == Macro) this.copy(macroVerified = v)
        else this
      case LarValidated(_, _) => this.copy(larCount = larCount + 1)
      case TsValidated(tSheet) => this.copy(ts = Some(tSheet))
    }

    def bothVerified: Boolean = qualityVerified && macroVerified
  }

}
