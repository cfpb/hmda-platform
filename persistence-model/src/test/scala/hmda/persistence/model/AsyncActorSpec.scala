package hmda.persistence.model

import akka.actor.ActorSystem
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }

class AsyncActorSpec extends AsyncWordSpec with MustMatchers with BeforeAndAfterAll {

  implicit lazy val system = ActorSystem()

  override def afterAll(): Unit = {
    system.terminate()
  }
}
