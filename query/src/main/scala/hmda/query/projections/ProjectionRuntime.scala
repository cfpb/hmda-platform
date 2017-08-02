package hmda.query.projections

import akka.actor.{ ActorSystem, Scheduler }
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

trait ProjectionRuntime {
  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val scheduler: Scheduler = system.scheduler
}
