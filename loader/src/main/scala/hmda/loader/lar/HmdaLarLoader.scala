package hmda.loader.lar

import akka.actor.{ActorPath, ActorRef, ActorSystem}
import akka.pattern.ask
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi.SubmissionId
import hmda.persistence.HmdaSupervisor.{FindHmdaFiling, FindProcessingActor, FindSubmissions}
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.processing.SubmissionManager
import scala.concurrent.duration._

object HmdaLarLoader extends App {

  val config = ConfigFactory.load()

  implicit val system = ActorSystem("hmda-cluster-client")
  implicit val ec = system.dispatcher

  val hmdaClusterName = config.getString("hmda.clusterName")
  val hmdaClusterIP = config.getString("hmda.lar.host")
  val hmdaClusterPort = config.getInt("hmda.lar.port")
  val actorTimeout = config.getInt("hmda.actorTimeout")

  implicit val timeout = Timeout(actorTimeout.seconds)

  val initialContacts = Set(
    ActorPath.fromString(s"akka.tcp://$hmdaClusterName@$hmdaClusterIP:$hmdaClusterPort/system/receptionist")
  )

  println(initialContacts)

  val settings = ClusterClientSettings(system)
    .withInitialContacts(initialContacts)

  val clusterClient = system.actorOf(ClusterClient.props(settings), "hmda-lar-loader")

  val institutionId = "institutionID"
  val period = "2017"

  val submissionId = SubmissionId(institutionId, period)

  val message = FindProcessingActor(SubmissionManager.name, submissionId)

  val fProcessingActor = (clusterClient ? ClusterClient.Send("/user/supervisor/singleton", message, localAffinity = true)).mapTo[ActorRef]
  val fSubmissionsActor = (clusterClient ? ClusterClient
    .Send("/user/supervisor/singleton",
      FindSubmissions(SubmissionPersistence.name, institutionId, period),
      localAffinity = true)).mapTo[ActorRef]

  (clusterClient ? ClusterClient.Send("/user/supervisor/singleton", FindHmdaFiling(period), localAffinity = true)).mapTo[ActorRef]
  //(clusterClient ? ClusterClient.Send("/user/query-supervisor", FindHmdaFilingView(period), localAffinity = true)).mapTo[ActorRef]




  private def uploadFile(): Unit = ???

}
