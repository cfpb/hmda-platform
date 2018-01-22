package hmda.persistence.eventAdapters

import akka.persistence.journal.{ EventAdapter, EventSeq }
import hmda.persistence.processing.HmdaFileValidator.{ HmdaFileValidationState, HmdaVerificationState }

/**
 * This class upgrades old events from the event journal
 *
 * actor -> message -> event adapter -> serializer -> journal
 * actor <- message <- event adapter <- serializer <- journal
 */
class HmdaValidationEventAdapter extends EventAdapter {
  override def manifest(event: Any): String = ""

  // Pass through to serializer since we always persist the latest events
  override def toJournal(event: Any): Any = event

  override def fromJournal(event: Any, manifest: String): EventSeq = {
    event match {
      case vs @ HmdaFileValidationState(ts, lars, tsSyntactical, tsValidity,
        tsQuality, larSyntactical, larValidity, larQuality,
        qualityVerified, larMacro, macroVerified) =>
        val sv = tsSyntactical.nonEmpty || larSyntactical.nonEmpty || tsValidity.nonEmpty || larValidity.nonEmpty
        val qm = tsQuality.nonEmpty || larQuality.nonEmpty || larMacro.nonEmpty
        val state = HmdaVerificationState(
          containsSVEdits = sv, containsQMEdits = qm,
          qualityVerified = qualityVerified, macroVerified = macroVerified
        )
        EventSeq(state)

      case verification: HmdaVerificationState =>
        EventSeq(verification)
    }
  }
}
