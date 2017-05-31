package hmda.query.serialization

import hmda.model.institution.InstitutionGenerators._
import hmda.query.view.institutions.InstitutionView.InstitutionViewState
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class InstitutionViewProtobufSerializerSpec extends PropSpec with PropertyChecks with MustMatchers {
  val serializer = new InstitutionViewProtobufSerializer()

  val institutionList = Gen.listOf(institutionGen)
  property("InstitutionViewState must serialize to protobuf and back") {
    forAll(institutionList, Gen.choose(0l, 100000l)) { (i, seq) =>
      val state = InstitutionViewState(i.toSet, seq)
      val bytes = serializer.toBinary(state)
      serializer.fromBinary(bytes, serializer.InstitutionViewStateManifest) mustBe state
    }
  }
}
