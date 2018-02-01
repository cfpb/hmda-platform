package hmda.persistence.serialization.processing

import hmda.model.fi.ts.TransmittalSheet
import hmda.persistence.messages.commands.processing.HmdaFileValidatorState.{ HmdaVerificationState, QMState, SVState }
import hmda.persistence.model.serialization.HmdaFileValidatorCommands.{ HmdaVerificationStateMessage, QMStateMessage, SVStateMessage }
import hmda.persistence.serialization.ts.TsProtobufConverter._

object HmdaFileValidatorStateProtobufConverter {

  def svStateToProtobuf(obj: SVState): SVStateMessage = {
    SVStateMessage(
      syntacticalEdits = obj.syntacticalEdits.toSeq,
      validityEdits = obj.validityEdits.toSeq
    )
  }

  def svStateFromProtobuf(msg: SVStateMessage): SVState = {
    SVState(
      syntacticalEdits = msg.syntacticalEdits.toSet,
      validityEdits = msg.validityEdits.toSet
    )
  }

  def qmStateToProtobuf(obj: QMState): QMStateMessage = {
    QMStateMessage(
      qualityEdits = obj.qualityEdits.toSeq,
      macroEdits = obj.macroEdits.toSeq
    )
  }

  def qmStateFromProtobuf(msg: QMStateMessage): QMState = {
    QMState(
      qualityEdits = msg.qualityEdits.toSet,
      macroEdits = msg.macroEdits.toSet
    )
  }

  def verificationStateToProtobuf(obj: HmdaVerificationState): HmdaVerificationStateMessage = {
    HmdaVerificationStateMessage(
      qualityVerified = obj.qualityVerified,
      macroVerified = obj.macroVerified,
      ts = obj.ts.map(t => tsToProtobuf(t)),
      larCount = obj.larCount
    )
  }

  def verificationStateFromProtobuf(msg: HmdaVerificationStateMessage): HmdaVerificationState = {
    HmdaVerificationState(
      qualityVerified = msg.qualityVerified,
      macroVerified = msg.macroVerified,
      ts = msg.ts.map(t => tsFromProtobuf(t)),
      larCount = msg.larCount
    )
  }
}
