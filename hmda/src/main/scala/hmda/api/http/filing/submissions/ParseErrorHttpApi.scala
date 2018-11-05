package hmda.api.http.filing.submissions

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import hmda.api.http.directives.HmdaTimeDirectives
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.model.filing.submission.SubmissionId

trait ParseErrorHttpApi {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  //institutions/<institution>/filings/<period>/submissions/<submissionId>/parseErrors
  val parseErrorPath =
    path(
      "institutions" / Segment / "filings" / Segment / "submissions" / IntNumber / "parseErrors") {
      (lei, period, seqNr) =>
        val submissionId = SubmissionId(lei, period, seqNr)
        complete("")
    }

}
