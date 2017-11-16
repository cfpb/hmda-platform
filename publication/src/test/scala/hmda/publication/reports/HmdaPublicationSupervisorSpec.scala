package hmda.publication.reports

import akka.actor.{ ActorRef, ActorSystem }
import akka.util.Timeout
import akka.pattern.ask
import hmda.publication.HmdaPublicationSupervisor
import hmda.publication.HmdaPublicationSupervisor.FindModifiedLarSubscriber
import hmda.publication.submission.lar.SubmissionSignedModifiedLarSubscriber
import org.scalatest.{ MustMatchers, WordSpec }

import scala.concurrent.Await
import scala.concurrent.duration._

class HmdaPublicationSupervisorSpec extends WordSpec with MustMatchers {

  val system = ActorSystem()

  val publicationSupervisor = system.actorOf(HmdaPublicationSupervisor.props(), HmdaPublicationSupervisor.name)

  implicit val ec = system.dispatcher
  val timeout = 2.seconds
  implicit val akkaTimeout = Timeout(timeout)

  "The HMDA Publication Supervisor" must {
    "Find or create modified lar subscriber" in {
      val path = s"akka://default/user/${HmdaPublicationSupervisor.name}/${SubmissionSignedModifiedLarSubscriber.name}"
      val fPublicationLar = (publicationSupervisor ? FindModifiedLarSubscriber).mapTo[ActorRef]
      val publicationLar = Await.result(fPublicationLar, timeout)
      publicationLar.path.toString mustBe path
    }

    "terminate ActorSystem" in {
      Thread.sleep(2000)
      system.terminate()
      system.whenTerminated.map { isTerminated =>
        isTerminated mustBe true
      }
    }
  }

}
