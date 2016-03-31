package hmda.persistence

import java.time.Instant

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import com.typesafe.config.ConfigFactory
import hmda.persistence.HmdaFileRaw.{ AddLine, GetState, HmdaFileRawState }
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpecLike }

class HmdaFileRawSpec(_system: ActorSystem)
    extends TestKit(_system)
    with WordSpecLike
    with ImplicitSender
    with MustMatchers
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("hmda-persistence-test"))

  val hmdaFileRaw = system.actorOf(HmdaFileRaw.props("1"))

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val data = "1|0123456789|9|201301171330|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
    "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
    "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
    "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4"

  val lines = data.split("\n")
  val timestamp = Instant.now.toEpochMilli

  "A HMDA File" must {
    "be persisted" in {
      for (line <- lines) {
        hmdaFileRaw ! AddLine(timestamp, line.toString)
      }
      hmdaFileRaw ! GetState
      expectMsg(HmdaFileRawState(Map(timestamp -> 4)))
    }
  }

}
