package hmda.api.processing

import java.time.Instant

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import hmda.api.processing.HmdaFileUpload.{ AddLine, GetState, HmdaFileUploadState }
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpecLike }

class HmdaFileUploadSpec(_system: ActorSystem)
    extends TestKit(_system)
    with WordSpecLike
    with ImplicitSender
    with MustMatchers
    with BeforeAndAfterAll {

  import hmda.parser.util.FITestData._

  def this() = this(ActorSystem("hmda-persistence-test"))

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val hmdaFileUpload = system.actorOf(HmdaFileUpload.props("1"))

  val lines = fiCSV.split("\n")
  val timestamp = Instant.now.toEpochMilli

  "A HMDA File" must {
    "be persisted" in {
      for (line <- lines) {
        hmdaFileUpload ! AddLine(timestamp, line.toString)
      }
      hmdaFileUpload ! GetState
      expectMsg(HmdaFileUploadState(Map(timestamp -> 4)))
    }
  }

}
