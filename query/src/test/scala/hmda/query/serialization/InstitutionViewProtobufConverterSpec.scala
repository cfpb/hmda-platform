package hmda.query.serialization

import hmda.model.institution.InstitutionGenerators._
import hmda.persistence.model.serialization.InstitutionViewEvents.InstitutionViewStateMessage
import hmda.query.view.institutions.InstitutionView.InstitutionViewState
import hmda.query.serialization.InstitutionViewProtobufConverter._
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class InstitutionViewProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers {
  val institutionList = Gen.listOf(institutionGen)

  property("InstitutionViewState must serialize to protobuf and back") {
    forAll(institutionList, Gen.choose(0l, 100000l)) { (i, seq) =>
      val state = InstitutionViewState(i.toSet, seq)
      val protobuf = institutionViewStateToProtobuf(state).toByteArray
      institutionViewStateFromProtobuf(InstitutionViewStateMessage.parseFrom(protobuf)) mustBe state
    }
  }

}
