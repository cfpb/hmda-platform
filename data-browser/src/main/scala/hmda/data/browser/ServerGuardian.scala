package hmda.data.browser

import akka.actor.{ActorSystem => UntypedActorSystem}
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl._
import akka.stream.scaladsl.Sink

import scala.concurrent.ExecutionContext
import akka.actor.typed.scaladsl.adapter._
import akka.stream.ActorMaterializer
import hmda.data.browser.models.Race.{Asian, TwoOrMoreMinorityRaces}
import hmda.data.browser.repositories.PostgresModifiedLarRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Await

object ServerGuardian {
  sealed trait Protocol
  private case class Ready(port: Int) extends Protocol
  private case class Error(errorMessage: String) extends Protocol

  def behavior: Behavior[Protocol] = Behaviors.setup { ctx =>
    implicit val untypedSystem: UntypedActorSystem =
      ctx.asScala.system.toUntyped

    implicit val mat: ActorMaterializer = ActorMaterializer()

    implicit val ec: ExecutionContext = ctx.executionContext

    val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("db")
    val repository =
      new PostgresModifiedLarRepository("modifiedlar2018", databaseConfig)

    import scala.concurrent.duration._
    println {
      Await.result(repository.findAndAggregate(35614, 5, Asian.entryName),
                   30.seconds)
    }

    Await.result(repository
                   .find("CA", 6, TwoOrMoreMinorityRaces.entryName)
                   .runWith(Sink.foreach(println)),
                 30.seconds)

    Await.result(repository
                   .find(1, TwoOrMoreMinorityRaces.entryName)
                   .runWith(Sink.foreach(println)),
                 30.seconds)

    Behaviors.stopped
  }
}