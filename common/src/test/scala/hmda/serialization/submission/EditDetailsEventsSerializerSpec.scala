package hmda.serialization.submission

import hmda.messages.submission.EditDetailsEvents.{
  EditDetailsAdded,
  EditDetailsRowCounted
}
import hmda.model.edits.EditDetailsGenerator.editDetailsGen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, PropSpec}

class EditDetailsEventsSerializerSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  val serializer = new EditDetailsEventsSerializer()

  property("EditDetailsAdded must serialize to and from binary") {
    forAll(editDetailsGen) { editDetails =>
      val editDetailsAdded = EditDetailsAdded(editDetails)
      val bytesCreated = serializer.toBinary(editDetailsAdded)
      serializer.fromBinary(bytesCreated, serializer.EditDetailsAddedManifest) mustBe editDetailsAdded
    }
  }

  property("EditDetailsRowCounted must serialize to and from binary") {
    forAll(editDetailsGen) { editDetails =>
      val editDetailsRowCounted = EditDetailsRowCounted(editDetails.rows.size)
      val bytesModified = serializer.toBinary(editDetailsRowCounted)
      serializer.fromBinary(
        bytesModified,
        serializer.EditDetailsRowCountedManifest) mustBe editDetailsRowCounted
    }
  }
}
