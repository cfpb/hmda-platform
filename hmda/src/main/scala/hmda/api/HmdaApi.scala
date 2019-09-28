package hmda.api

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import hmda.api.http.HmdaFilingApi
//import hmda.api.http.{ HmdaAdminApi, HmdaFilingApi, HmdaPublicApi }
import hmda.api.ws.HmdaWSApi

object HmdaApi {
  final val name = "HmdaApi"

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    //    ctx.actorOf(HmdaAdminApi.props, HmdaAdminApi.adminApiName)
    //    ctx.actorOf(HmdaPublicApi.props, HmdaPublicApi.publicApiName)
    ctx.spawn[Nothing](HmdaFilingApi(), HmdaFilingApi.name)
    ctx.spawn[Nothing](HmdaWSApi(), HmdaWSApi.name)

    Behaviors.empty
  }
}