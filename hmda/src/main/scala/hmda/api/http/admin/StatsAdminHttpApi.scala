package hmda.api.http.admin

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.Timeout
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.{ SignedLeiCountResponse, SignedLeiListResponse, TotalLeiCountResponse, TotalLeiListResponse }
import hmda.auth.OAuth2Authorization
import hmda.messages.filing.FilingCommands.GetLatestSignedSubmission
import hmda.model.filing.submission.Submission
import hmda.persistence.filing.FilingPersistence
import hmda.persistence.institution.InstitutionPersistence
import hmda.query.HmdaQuery
import hmda.utils.YearUtils
import hmda.utils.YearUtils.Period
import org.slf4j.Logger

import scala.concurrent.{ ExecutionContext, Future }

object StatsAdminHttpApi {
  def create(log: Logger, config: Config, clusterSharding: ClusterSharding)(
    implicit t: Timeout,
    ec: ExecutionContext,
    system: ActorSystem[_],
    mat: Materializer
  ): OAuth2Authorization => Route =
    new StatsAdminHttpApi(log, config, clusterSharding)(t, ec, system, mat).routes
}

private class StatsAdminHttpApi(log: Logger, config: Config, clusterSharding: ClusterSharding)(
  implicit t: Timeout,
  ec: ExecutionContext,
  system: ActorSystem[_],
  mat: Materializer
) {
  private val hmdaAdminRole: String = config.getString("keycloak.hmda.admin.role")

  val routes: OAuth2Authorization => Route = { (oauth2Authorization: OAuth2Authorization) =>
    val getLeisCountWithSignedFilling =
      get
        .&(path("admin" / "count" / "lei" / Segment / "filed"))
        .&(oauth2Authorization.authorizeTokenWithRole(hmdaAdminRole)) { (periodStr, _) =>
          val period = YearUtils.parsePeriod(periodStr).toTry.get
          val result = getSignedSubmissionsStream(period)
            .map(_ => 1)
            .fold(0)(_ + _)
            .runWith(Sink.head)
            .map(SignedLeiCountResponse.apply)
          complete(result)
        }

    val getLeisListWithSignedFilling =
      get
        .&(path("admin" / "list" / "lei" / Segment / "filed"))
        .&(oauth2Authorization.authorizeTokenWithRole(hmdaAdminRole)) { (periodStr, _) =>
          val period = YearUtils.parsePeriod(periodStr).toTry.get
          val result: Future[SignedLeiListResponse] =
            getSignedSubmissionsStream(period)
              .map({ case (lei, sub) => SignedLeiListResponse.Elem(lei, sub.id.toString) })
              .runWith(Sink.seq)
              .map(SignedLeiListResponse.apply)
          complete(result)
        }

    val getTotalLeisCount =
      get
        .&(path("admin" / "count" / Segment / "lei"))
        .&(oauth2Authorization.authorizeTokenWithRole(hmdaAdminRole)) { (periodStr, _) =>
          val period = YearUtils.parsePeriod(periodStr).toTry.get
          val result = getTotalLeiStream(period)
            .map(_ => 1)
            .fold(0)(_ + _)
            .runWith(Sink.head)
            .map(TotalLeiCountResponse.apply)
          complete(result)
        }

    val getTotalLeisList =
      get
        .&(path("admin" / "list" / Segment / "lei"))
        .&(oauth2Authorization.authorizeTokenWithRole(hmdaAdminRole)) { (periodStr, _) =>
          val period = YearUtils.parsePeriod(periodStr).toTry.get
          val result = getTotalLeiStream(period)
            .map(_.lei)
            .runWith(Sink.seq)
            .map(TotalLeiListResponse.apply)
          complete(result)
        }

    getLeisCountWithSignedFilling ~
      getLeisListWithSignedFilling ~
      getTotalLeisCount ~
      getTotalLeisList
  }

  // stream of (lei -> last signed submission)
  private def getSignedSubmissionsStream(period: Period): Source[(String, Submission), NotUsed] =
    HmdaQuery
      .readJournal(system)
      .currentPersistenceIds()
      .mapConcat(x => FilingPersistence.parseEntityId(x).toList)
      .filter(id => id.period == period)
      .mapAsync(1)({ id =>
        val fillingRef = clusterSharding.entityRefFor(FilingPersistence.typeKey, id.mkString)
        for {
          signedSubmissionOpt <- fillingRef ? (ref => GetLatestSignedSubmission(ref))
        } yield signedSubmissionOpt.map(sub => id.lei -> sub)
      })
      .mapConcat(_.toList)

  private def getTotalLeiStream(period: Period): Source[InstitutionPersistence.EntityId, NotUsed] =
    HmdaQuery
      .readJournal(system)
      .currentPersistenceIds()
      .mapConcat(x => InstitutionPersistence.parseEntityId(x).toList)
      .filter(id => id.year == period.year)
}