package hmda.persistence.serialization.submission

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.institution.SubmissionGenerators._
import hmda.persistence.messages.events.institutions.SubmissionEvents._
import org.scalacheck.Gen

class SubmissionProtobufSerializerSpec extends PropSpec with PropertyChecks with MustMatchers {
  val serializer = new SubmissionProtobufSerializer()

  property("Submission Created messages must be serialized to binary and back") {
    forAll(submissionGen) { submission =>
      val msg = SubmissionCreated(submission)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.SubmissionCreatedManifest) mustBe msg
    }
  }

  property("Submission Status Updated messages must be serialized to binary and back") {
    forAll(submissionIdGen, submissionStatusGen) { (submissionId, submissionStatus) =>
      val msg = SubmissionStatusUpdated(submissionId, submissionStatus)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.SubmissionStatusUpdatedManifest) mustBe msg
    }
  }

  property("Submission File Name Added messages must be serialized to binary and back") {
    forAll(submissionIdGen, Gen.alphaStr) { (submissionId, fileName) =>
      val msg = SubmissionFileNameAdded(submissionId, fileName)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.SubmissionFileNameAddedManifest) mustBe msg
    }
  }
}
