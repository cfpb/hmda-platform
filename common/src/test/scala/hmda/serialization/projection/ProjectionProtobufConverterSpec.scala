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
    val probe = TestProbe[OffsetSaved](name = "projection-command")
    "convert SaveOffset to and from protobuf" in {
      val save = SaveOffset(1L, probe.ref)
      val protobuf = saveOffsetToProtobuf(save, resolver).toByteArray
      saveOffsetFromProtobuf(protobuf, resolver) mustBe save
    }

    "convert GetOffset to and from protobuf" in {
      val get = GetOffset(probe.ref)
      val protobuf = getOffsetToProtobuf(get, resolver).toByteArray
      getOffsetFromProtobuf(protobuf, resolver) mustBe get
    }

    "convert OffsetSaved to and from protobuf" in {
      val saved = OffsetSaved(1L)
      val protobuf = offsetSavedToProtobuf(saved)
      offsetSavedFromProtobuf(protobuf) mustBe saved
    }
  }

}
