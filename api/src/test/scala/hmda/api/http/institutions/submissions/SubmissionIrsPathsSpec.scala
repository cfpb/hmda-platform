package hmda.api.http.institutions.submissions

import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.http.{ InstitutionHttpApiAsyncSpec, InstitutionHttpApiSpec }
import hmda.model.fi.lar.LarGenerators
import hmda.query.DbConfiguration._
import hmda.query.model.filing.Irs
import hmda.query.repository.filing.{ FilingComponent, LarConverter }

import scala.concurrent.Await

class SubmissionIrsPathsSpec
    extends InstitutionHttpApiSpec
    with FilingComponent
    with LarGenerators {

  import LarConverter._
  import config.profile.api._

  val repository = new LarRepository(config)
  val larTotalMsaRepository = new LarTotalMsaRepository(config)
  val modifiedLarRepository = new ModifiedLarRepository(config)

  override def beforeAll(): Unit = {
    super.beforeAll()
    dropAllObjects()
    Await.result(repository.createSchema(), duration)
    Await.result(larTotalMsaRepository.createSchema(), duration)
    Await.result(modifiedLarRepository.createSchema(), duration)
    //loadData()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    dropAllObjects()
  }

  private def loadData() = {
    val msa1 = geographyGen.sample.get.copy(msa = "12345")
    val msaNa = geographyGen.sample.get.copy(msa = "NA")
    val loan = loanGen.sample.get.copy(amount = 12)
    val lar1 = toLoanApplicationRegisterQuery(larGen.sample.get.copy(geography = msa1, loan = loan)).copy(institutionId = "0")
    val lar2 = toLoanApplicationRegisterQuery(larGen.sample.get.copy(geography = msaNa, loan = loan)).copy(institutionId = "0")
    val query1 = lar1.copy(period = "2017")
    val query2 = lar2.copy(period = "2017")

    Await.result(repository.insertOrUpdate(query1), duration)
    Await.result(repository.insertOrUpdate(query2), duration)
  }

  private def dropAllObjects() = {
    val db = repository.config.db
    val dropAll = sqlu"""DROP ALL OBJECTS"""
    Await.result(db.run(dropAll), duration)
  }

  "Submission Irs Paths" must {
    "return a 200" in {
      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/irs") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        /*val irs = responseAs[Irs]
        irs.totals.amount mustBe 24
        irs.totals.lars mustBe 2
        irs.msas.length mustBe 2
        Thread.sleep(1000)*/
      }
    }
  }
}
