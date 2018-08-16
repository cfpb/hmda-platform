package hmda.serialization.projection

import akka.actor.ActorSystem
import hmda.messages.projection.CommonProjectionMessages.{
  GetOffset,
  OffsetSaved,
  SaveOffset
}
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpec}
import akka.actor.typed.scaladsl.adapter._
import ProjectionProtobufConverter._
import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.ActorRefResolver
import akka.persistence.query.TimeBasedUUID
import com.datastax.driver.core.utils.UUIDs

class ProjectionProtobufConverterSpec
    extends WordSpec
    with MustMatchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate()
  }

  implicit val system = ActorSystem()
  implicit val systemTyped = system.toTyped
  private val resolver = ActorRefResolver(systemTyped)

  "Projection Protobuf Converter" must {
    val uuid = UUIDs.timeBased()
    val offset = TimeBasedUUID(uuid)
    val probe = TestProbe[OffsetSaved](name = "projection-command")
    "convert SaveOffset to and from protobuf" in {
      val save = SaveOffset(offset, probe.ref)
      val protobuf = saveOffsetToProtobuf(save, resolver).toByteArray
      saveOffsetFromProtobuf(protobuf, resolver) mustBe save
    }

    "convert GetOffset to and from protobuf" in {
      val get = GetOffset(probe.ref)
      val protobuf = getOffsetToProtobuf(get, resolver).toByteArray
      getOffsetFromProtobuf(protobuf, resolver) mustBe get
    }

    "convert OffsetSaved to and from protobuf" in {
      val uuid = UUIDs.timeBased()
      val offset = TimeBasedUUID(uuid)
      val saved = OffsetSaved(offset)
      val protobuf = offsetSavedToProtobuf(saved)
      offsetSavedFromProtobuf(protobuf) mustBe saved
    }
  }

}
