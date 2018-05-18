package hmda.api.http

import akka.actor.Props
import hmda.actor.HmdaActor

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
