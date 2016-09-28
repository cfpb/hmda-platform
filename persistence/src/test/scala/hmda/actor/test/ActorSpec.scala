package hmda.actor.test

import akka.actor.ActorSystem
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers, WordSpec }

class ActorSpec extends AsyncWordSpec with MustMatchers with BeforeAndAfterAll {

  implicit lazy val system = ActorSystem()

  override def afterAll(): Unit = {
    system.terminate()
  }

}
