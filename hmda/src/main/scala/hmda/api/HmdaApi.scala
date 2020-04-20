package hmda.api

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import hmda.api.http.{ HmdaAdminApi, HmdaFilingApi }
import hmda.api.ws.HmdaWSApi

object HmdaApi {
  val name = "HmdaApi"

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    //    ctx.actorOf(HmdaPublicApi.props, HmdaPublicApi.publicApiName)
    ctx.spawn[Nothing](HmdaAdminApi(), HmdaAdminApi.name)
    ctx.spawn[Nothing](HmdaFilingApi(), HmdaFilingApi.name)
    ctx.spawn[Nothing](HmdaWSApi(), HmdaWSApi.name)

    Behaviors.empty
  }
}