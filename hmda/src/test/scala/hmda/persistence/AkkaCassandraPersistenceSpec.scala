package hmda.persistence

import java.time.Instant
import java.util.concurrent.TimeUnit

import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import akka.persistence.typed.scaladsl.{Effect, PersistentBehaviors}
import akka.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import hmda.persistence.util.CassandraUtil
import org.scalatest.{BeforeAndAfterAll, WordSpec}

import scala.concurrent.duration._

abstract class AkkaCassandraPersistenceSpec
    extends WordSpec
    with ActorTestKit
    with BeforeAndAfterAll {

  sealed trait Command
  sealed trait Event
  case class Request(replyTo: ActorRef[Event]) extends Command
  case object Response extends Event

  override def beforeAll(): Unit = {
    CassandraUtil.startEmbeddedCassandra()
    awaitPersistenceInit()
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    CassandraUtil.shutdown()
    shutdownTestKit()
    super.afterAll()
  }

  def awaitPersistenceInit(): Unit = {
    val id = Instant.now().toEpochMilli
    val probe = TestProbe[Event](s"probe-$id")
    val t0 = System.nanoTime()

    probe.within(45.seconds) {
      probe.awaitAssert {
        val actor = spawn(AwaitPersistenceInit.behavior)
        actor ! Request(probe.ref)
        probe.expectMessage(5.seconds, Response)
        system.log.debug("awaitPersistenceInit took {} ms {}",
                         TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0),
                         system.name)
      }
    }
  }

  object AwaitPersistenceInit {

    case class AwaitState(nr: Int = 0)

    def behavior: Behavior[Command] =
      PersistentBehaviors
        .receive[Command, Event, AwaitState](
          persistenceId = s"await-persistence-id",
          initialState = AwaitState(),
          commandHandler = commandHandler,
          eventHandler = eventHandler
        )

    val commandHandler: CommandHandler[Command, Event, AwaitState] = {
      (ctx, _, cmd) =>
        cmd match {
          case Request(replyTo) =>
            Effect.persist(Response).andThen {
              ctx.log.debug(s"Persisted: $cmd")
              replyTo ! Response
            }

        }
    }

    val eventHandler: (AwaitState, Event) => (AwaitState) = {
      case (state, Response) => state.copy(nr = state.nr + 1)
    }

  }

}
