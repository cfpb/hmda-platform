package hmda.persistence.serialization.processing

import hmda.model.fi.ts.TsGenerators
import hmda.persistence.messages.commands.processing.HmdaFileValidatorState.{ HmdaVerificationState, QMState, SVState }
import org.scalacheck.Gen

object HmdaFileValidatorStateGenerators extends TsGenerators {

  implicit def svStateGen: Gen[SVState] = {
    for {
      syntacticalEdits <- Gen.listOf(Gen.alphaNumStr)
      validityEdits <- Gen.listOf(Gen.alphaNumStr)
    } yield SVState(syntacticalEdits.toSet, validityEdits.toSet)
  }

  implicit def qmStateGen: Gen[QMState] = {
    for {
      qualityEdits <- Gen.listOf(Gen.alphaNumStr)
      macroEdits <- Gen.listOf(Gen.alphaNumStr)
    } yield QMState(qualityEdits.toSet, macroEdits.toSet)
  }

  implicit def verificationStateGen: Gen[HmdaVerificationState] = {
    for {
      qualityVerified <- Gen.oneOf(true, false)
      macroVerified <- Gen.oneOf(true, false)
      ts <- Gen.some(tsGen)
      larCount <- Gen.choose(0, Int.MaxValue)
    } yield HmdaVerificationState(qualityVerified, macroVerified, ts, larCount)
  }

}
