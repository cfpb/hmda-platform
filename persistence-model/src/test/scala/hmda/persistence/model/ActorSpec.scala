package hmda.persistence.model

import akka.actor.ActorSystem
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }

class ActorSpec extends WordSpec with MustMatchers with BeforeAndAfterAll {

  implicit lazy val system = ActorSystem()

  override def afterAll(): Unit = {
    system.terminate()
  }

}
