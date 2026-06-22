package hmda.publisher.scheduler

import org.apache.pekko.actor.ActorRef
// $COVERAGE-OFF$
case class AllSchedulers(
                          combinedMLarPublicScheduler: ActorRef,
                          larPublicScheduler: ActorRef,
                          larScheduler: ActorRef,
                          panelScheduler: ActorRef,
                          tsPublicScheduler: ActorRef,
                          tsScheduler: ActorRef
                        )
// $COVERAGE-ON$