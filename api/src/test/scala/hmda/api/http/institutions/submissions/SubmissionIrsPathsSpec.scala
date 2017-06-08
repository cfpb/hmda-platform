package hmda.api.http.institutions.submissions

import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.http.InstitutionHttpApiSpec
import hmda.api.model.IrsResponse
import hmda.census.model.Msa
import hmda.model.fi.SubmissionId
import hmda.persistence.model.MsaGenerators
import hmda.validation.ValidationStats._

class SubmissionIrsPathsSpec
    extends InstitutionHttpApiSpec
    with MsaGenerators {

  val subId = SubmissionId("0", "2017", 1)

  val list = listOfMsaGen.sample.getOrElse(List[Msa]())

  override def beforeAll(): Unit = {
    super.beforeAll()
    validationStats ! AddIrsStats(list, subId)
  }

  "Submission Irs Paths" must {
    "return a 200" in {
      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/irs") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        val irs = responseAs[IrsResponse]
        irs.currentPage mustBe 1
        irs.summary.amount mustBe list.map(_.totalAmount).sum
        irs.summary.lars mustBe list.map(_.totalLars).sum
      }
    }
  }
}
