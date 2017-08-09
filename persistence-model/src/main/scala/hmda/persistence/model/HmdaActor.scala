package hmda.persistence.model

import akka.actor.{ Actor, ActorLogging }

trait HmdaActor extends Actor with ActorLogging {

  override def preStart(): Unit = {
    log.info(s"Actor started at ${self.path}")
    log.info("Thread name for actor: " + Thread.currentThread().getName)
  }

  override def postStop(): Unit = {
    log.info(s"Actor stopped at ${self.path}")
  }

}
