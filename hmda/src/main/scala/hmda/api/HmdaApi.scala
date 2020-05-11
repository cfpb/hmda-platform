package hmda.api

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import hmda.api.http.{ HmdaAdminApi, HmdaFilingApi, HmdaPublicApi }
import hmda.api.ws.HmdaWSApi

// This is just a Guardian for starting up APIs
// $COVERAGE-OFF$
object HmdaApi {
  val name = "HmdaApi"

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    ctx.spawn[Nothing](HmdaPublicApi(), HmdaPublicApi.name)
    ctx.spawn[Nothing](HmdaAdminApi(), HmdaAdminApi.name)
    ctx.spawn[Nothing](HmdaFilingApi(), HmdaFilingApi.name)
    ctx.spawn[Nothing](HmdaWSApi(), HmdaWSApi.name)

    Behaviors.empty
  }
}
// $COVERAGE-OFF$