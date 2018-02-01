package hmda.persistence.serialization.processing

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import HmdaFileValidatorStateGenerators._
import HmdaFileValidatorStateProtobufConverter._
import hmda.persistence.model.serialization.HmdaFileValidatorCommands.{ HmdaVerificationStateMessage, QMStateMessage, SVStateMessage }

class HmdaFileValidatorStateProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("SVState must serialize to protobuf and back") {
    forAll(svStateGen) { svState =>
      val protobuf = svStateToProtobuf(svState).toByteArray
      svStateFromProtobuf(SVStateMessage.parseFrom(protobuf)) mustBe svState
    }
  }

  property("QMState must serialize to protobuf and back") {
    forAll(qmStateGen) { qmState =>
      val protobuf = qmStateToProtobuf(qmState).toByteArray
      qmStateFromProtobuf(QMStateMessage.parseFrom(protobuf)) mustBe qmState
    }
  }

  property("HmdaVerificationState must serialize to protobuf and back") {
    forAll(verificationStateGen) { verificationState =>
      val protobuf = verificationStateToProtobuf(verificationState).toByteArray
      verificationStateFromProtobuf(HmdaVerificationStateMessage.parseFrom(protobuf)) mustBe verificationState
    }
  }

}
