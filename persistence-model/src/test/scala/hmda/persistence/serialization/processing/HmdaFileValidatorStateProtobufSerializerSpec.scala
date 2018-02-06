package hmda.persistence.serialization.processing

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import HmdaFileValidatorStateGenerators._

class HmdaFileValidatorStateProtobufSerializerSpec extends PropSpec with PropertyChecks with MustMatchers {

  val serializer = new HmdaFileValidatorStateProtobufSerializer()

  property("SVState messages must be serialized to binary and back") {
    forAll(svStateGen) { svState =>
      val bytes = serializer.toBinary(svState)
      serializer.fromBinary(bytes, serializer.SVStateManifest) mustBe svState
    }
  }

  property("QMState messages must be serialized to binary and back") {
    forAll(qmStateGen) { qmState =>
      val bytes = serializer.toBinary(qmState)
      serializer.fromBinary(bytes, serializer.QMStateManifest) mustBe qmState
    }
  }

  property("HmdaVerificationState messages must be serialized to binary and back") {
    forAll(verificationStateGen) { verificationState =>
      val bytes = serializer.toBinary(verificationState)
      serializer.fromBinary(bytes, serializer.VerificationManifest) mustBe verificationState
    }
  }
}
