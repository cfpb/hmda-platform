package hmda.api.http

import akka.actor.Props
import hmda.model.actor.HmdaActor

object HmdaApi {
  final val name = "HmdaApi"
  def props: Props = Props(new HmdaApi)
}

class HmdaApi extends HmdaActor {

  val filingApi = context.actorOf(HmdaFilingApi.props)
  val adminApi = context.actorOf(HmdaAdminApi.props)
  val publicApi = context.actorOf(HmdaPublicApi.props)
  val wsApi = context.actorOf(HmdaWSApi.props)

}
