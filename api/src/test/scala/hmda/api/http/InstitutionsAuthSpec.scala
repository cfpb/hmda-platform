package hmda.api.http

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import hmda.model.institution.Agency.CFPB
import hmda.model.institution.ExternalIdType.UndeterminedExternalId
import hmda.model.institution.InstitutionType.MBS
import hmda.model.institution._
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.institutions.InstitutionPersistence.CreateInstitution
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName

import scala.concurrent.Future

class InstitutionsAuthSpec extends InstitutionHttpApiSpec {

  "Institutions API Authorization and rejection handling" must {

    val sealedRoute = Route.seal(institutionsRoutes)

    // Require 'CFPB-HMDA-Username' header
    // Request these endpoints without username header (but with other required headers)
    "reject requests to /institutions without 'CFPB-HMDA-Username' header" in {
      Get("/institutions").addHeader(institutionsHeader) ~> sealedRoute ~> check {
        status mustBe StatusCodes.Forbidden
      }
    }
    "reject requests to /inst/id without 'CFPB-HMDA-Username' header" in {
      Get("/institutions/0").addHeader(institutionsHeader) ~> sealedRoute ~> check {
        status mustBe StatusCodes.Forbidden
      }
    }
    "reject requests to /inst/id/filings/p without 'CFPB-HMDA-Username' header" in {
      Get("/institutions/0/filings/2017").addHeader(institutionsHeader) ~> sealedRoute ~> check {
        status mustBe StatusCodes.Forbidden
      }
    }

    // Require 'CFPB-HMDA-Institutions' header
    // Request these endpoints without institutions header (but with other required headers)
    "reject requests to /inst without 'CFPB-HMDA-Institutions' header" in {
      Get("/institutions").addHeader(usernameHeader) ~> sealedRoute ~> check {
        status mustBe StatusCodes.Forbidden
      }
    }
    "reject requests to submission creation without 'CFPB-HMDA-Institutions' header" in {
      Post("/institutions/0/filings/2017/submissions").addHeader(usernameHeader) ~> sealedRoute ~> check {
        status mustBe StatusCodes.Forbidden
      }
    }
    "reject requests to submission summary without 'CFPB-HMDA-Institutions' header" in {
      Get("/institutions/0/filings/2017").addHeader(usernameHeader) ~> sealedRoute ~> check {
        status mustBe StatusCodes.Forbidden
      }
    }

    // 'CFPB-HMDA-Institutions' header must match requested institution
    // Request these endpoints with all required headers, but request an institutionId that
    //   is not included in RequestHeaderUtils institutionsHeader
    "reject requests to /filings/period when institutionId in path is not included in 'CFPB-HMDA-Institutions' header" in {
      getWithCfpbHeaders("/institutions/1345/filings/2017") ~> sealedRoute ~> check {
        status mustBe StatusCodes.Forbidden
      }
    }
    "reject requests to /upload when institutionId in path is not included in 'CFPB-HMDA-Institutions' header" in {
      val csv = "1|0123456789|9|201301171330|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
        "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
        "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4"
      val file = multiPartFile(csv, "unauthorized_sample.txt")

      postWithCfpbHeaders("/institutions/1245/filings/2017/submissions/1", file) ~> sealedRoute ~> check {
        status mustBe StatusCodes.Forbidden
      }
    }

    "match 'CFPB-HMDA-Institutions' header case insensitively" in {
      val respondent = Respondent(ExternalId("1", UndeterminedExternalId), "test bank", "", "", "")
      val parent = Parent("123", 123, "test parent", "", "")
      val topHolder = TopHolder(-1, "", "", "", "")
      val caseInsensitiveBank = Institution("1", CFPB, 2017, MBS, cra = true, Set(), Set(), respondent = respondent, hmdaFilerFlag = true, parent = parent, assets = 0, otherLenderCode = 0, topHolder = topHolder)
      val querySupervisor = system.actorSelection("/user/query-supervisor")
      val fInstitutionsActor = (supervisor ? FindActorByName(InstitutionPersistence.name)).mapTo[ActorRef]

      val fInstitutions: Future[Unit] = for {
        h <- fInstitutionsActor
      } yield {
        h ! CreateInstitution(caseInsensitiveBank)
      }

      val institutionLower = "abc"
      val institutionUpper = "ABC"
      val instHeader = RawHeader("CFPB-HMDA-Institutions", institutionUpper)

      fInstitutions.map { i =>
        Get(s"/institutions/$institutionLower")
          .addHeader(usernameHeader)
          .addHeader(instHeader) ~> institutionsRoutes ~> check {
            status mustBe StatusCodes.OK
          }
      }
    }

    // Other auth
    "reject unauthorized requests to any /institutions-based path, even nonexistent endpoints" in {
      // Request the endpoint without a required header
      Get("/institutions/0/nonsense").addHeader(usernameHeader) ~> sealedRoute ~> check {
        status mustBe StatusCodes.Forbidden
      }
    }
    "not handle requests to nonexistent endpoints if the request is authorized" in {
      getWithCfpbHeaders("/lars") ~> sealedRoute ~> check {
        status mustBe StatusCodes.NotFound
      }
    }
    "accept headers case-insensitively" in {
      val usernameLower = RawHeader("cfpb-hmda-username", "someuser")
      val institutionsUpper = RawHeader("CFPB-HMDA-INSTITUTIONS", "1,2,3")

      Get("/institutions").addHeader(usernameLower).addHeader(institutionsUpper) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
      }
    }

  }

}
