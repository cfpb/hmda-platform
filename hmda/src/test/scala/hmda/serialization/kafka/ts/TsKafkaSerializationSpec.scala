package hmda.serialization.kafka.ts

import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, PropSpec}
import hmda.model.filing.ts.TsGenerators._

class TsKafkaSerializationSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {
  val serializer = new TsKafkaSerializer
  val deserializer = new TsKafkaDeserializer

  property("Transmittal Sheet must serialize / deserialize for Kafka") {
    forAll(tsGen) { ts =>
      val serialized = serializer.serialize("topic", ts)
      deserializer.deserialize("topic", serialized) mustBe ts
    }
  }
}
