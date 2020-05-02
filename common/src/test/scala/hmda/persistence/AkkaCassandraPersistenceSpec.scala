package hmda.persistence

import java.time.Instant
import java.util.concurrent.TimeUnit

import akka.actor
import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior, TypedActorContext }
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior.CommandHandler
import akka.persistence.typed.scaladsl.{ Effect, EventSourcedBehavior }
import hmda.persistence.util.CassandraUtil
import org.scalacheck.Gen
import org.scalatest.{ BeforeAndAfterAll, WordSpec }

import scala.concurrent.duration._

abstract class AkkaCassandraPersistenceSpec extends WordSpec with BeforeAndAfterAll {

  sealed trait Command
  sealed trait Event
  case class Request(replyTo: ActorRef[Event]) extends Command
  case object Response                         extends Event

  implicit val system: actor.ActorSystem
  implicit val typedSystem: ActorSystem[_]

  override def beforeAll(): Unit = {
    CassandraUtil.startEmbeddedCassandra()
    awaitPersistenceInit()
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    CassandraUtil.shutdown()
    system.terminate()
    super.afterAll()
  }

  def awaitPersistenceInit(): Unit = {
    val id    = Instant.now().toEpochMilli
    val probe = TestProbe[Event](s"probe-$id")
    val t0    = System.nanoTime()

    probe.within(45.seconds) {
      probe.awaitAssert {
        val actor =
          system.spawn(AwaitPersistenceInit.behavior, actorName)
        actor ! Request(probe.ref)
        probe.expectMessage(5.seconds, Response)
        system.log.debug("awaitPersistenceInit took {} ms {}", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0), system.name)
      }
    }
  }

  object AwaitPersistenceInit {

    final val name = "AwaitPersistenceInit"

    case class AwaitState(nr: Int = 0)

    def behavior: Behavior[Command] =
      Behaviors.setup { ctx =>
        EventSourcedBehavior[Command, Event, AwaitState](
          persistenceId = PersistenceId.ofUniqueId("await-persistence-id"),
          emptyState = AwaitState(),
          commandHandler = commandHandler(ctx),
          eventHandler = eventHandler
        )
      }

    def commandHandler(ctx: TypedActorContext[Command]): CommandHandler[Command, Event, AwaitState] = { (_, cmd) =>
      val log = ctx.asScala.log
      cmd match {
        case Request(replyTo) =>
          Effect.persist(Response).thenRun { _ =>
            log.debug(s"Persisted: $cmd")
            replyTo ! Response
          }

      }
    }

    val eventHandler: (AwaitState, Event) => AwaitState = {
      case (state, Response) => state.copy(nr = state.nr + 1)
      case _                 => AwaitState()
    }

  }

  protected def actorName: String = {
    val now = Instant.now().toEpochMilli
    Gen.alphaStr.suchThat(s => s != "").sample.getOrElse(s"name-$now")
  }

}