package hmda.api.processing

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpecLike }

class ActorSpec(_system: ActorSystem)
    extends TestKit(_system)
    with WordSpecLike
    with ImplicitSender
    with MustMatchers
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("hmda-test"))

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

}
