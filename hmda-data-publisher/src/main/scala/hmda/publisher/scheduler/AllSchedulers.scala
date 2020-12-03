package hmda.publisher.scheduler

import akka.actor.ActorRef

case class AllSchedulers(
                          larPublicScheduler: ActorRef,
                          larScheduler: ActorRef,
                          panelScheduler: ActorRef,
                          tsPublicScheduler: ActorRef,
                          tsScheduler: ActorRef
                        )