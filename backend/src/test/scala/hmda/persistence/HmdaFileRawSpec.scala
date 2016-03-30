package hmda.persistence

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}

class HmdaFileRawSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with MustMatchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("hmda", ConfigFactory.parseString(
    """
      |akka.loglevel = "DEBUG"
      |akka.persistence.journal.plugin = "in-memory-journal"
    """.stripMargin)


  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }



}
