package hmda.publication.lar.sender

import akka.actor.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.typesafe.config.ConfigFactory
import hmda.model.filing.submission.SubmissionId

import scala.concurrent.ExecutionContext

sealed trait EmailCommand


object EmailSender {

  final val name: String = "EmailSender"

  val config = ConfigFactory.load()

  def behavior(): Behavior[EmailCommand] = Behaviors.setup {ctx =>

    val log = ctx.log


  }

}
