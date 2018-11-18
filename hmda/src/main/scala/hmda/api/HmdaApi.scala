package hmda.api

import akka.actor.Props
import hmda.actor.HmdaActor
import hmda.api.http.{HmdaAdminApi, HmdaFilingApi, HmdaPublicApi}
import hmda.api.ws.HmdaWSApi

object HmdaApi {
  final val name = "HmdaApi"
  def props: Props = Props(new HmdaApi)
}

class HmdaApi extends HmdaActor {

  val filingApi =
    context.actorOf(HmdaFilingApi.props, HmdaFilingApi.filingApiName)
  val adminApi = context.actorOf(HmdaAdminApi.props, HmdaAdminApi.adminApiName)
  val publicApi =
    context.actorOf(HmdaPublicApi.props, HmdaPublicApi.publicApiName)
  val wsApi = context.actorOf(HmdaWSApi.props, HmdaWSApi.wsApiName)

}
