package hmda.api.http.institutions.submissions

import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.http.InstitutionHttpApiAsyncSpec
import hmda.api.model.IrsResponse
import hmda.model.fi.lar.LarGenerators
import hmda.query.DbConfiguration._
import hmda.query.repository.filing.{ FilingComponent, LarConverter }

import scala.concurrent.{ Await, Future }
import scala.util.{ Failure, Success }

class SubmissionIrsPathsSpec
    extends InstitutionHttpApiAsyncSpec
    with FilingComponent
    with LarGenerators {

  import LarConverter._
  import config.profile.api._

  val repository = new LarRepository(config)
  val larTotalMsaRepository = new LarTotalMsaRepository(config)
  val modifiedLarRepository = new ModifiedLarRepository(config)

  override def beforeAll(): Unit = {
    super.beforeAll()

    val setup = for {
      a <- dropAllObjects
      b <- repository.createSchema()
      c <- larTotalMsaRepository.createSchema()
      d <- modifiedLarRepository.createSchema()
      e <- addL1
      f <- addL2
    } yield (e, f)
    Await.result(setup, duration)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    dropAllObjects
  }

  private def addL1: Future[Int] = {
    val msa1 = geographyGen.sample.get.copy(msa = "12345")
    val loan = loanGen.sample.get.copy(amount = 12)
    val lar1 = toLoanApplicationRegisterQuery(larGen.sample.get.copy(geography = msa1, loan = loan)).copy(institutionId = "0")
    val query1 = lar1.copy(period = "2017")
    repository.insertOrUpdate(query1)
  }

  private def addL2: Future[Int] = {
    val msaNa = geographyGen.sample.get.copy(msa = "NA")
    val loan = loanGen.sample.get.copy(amount = 12)
    val lar2 = toLoanApplicationRegisterQuery(larGen.sample.get.copy(geography = msaNa, loan = loan)).copy(institutionId = "0")
    val query2 = lar2.copy(period = "2017")
    repository.insertOrUpdate(query2)
  }

  private def dropAllObjects: Future[Int] = {
    val db = repository.config.db
    val dropAll = sqlu"""DROP ALL OBJECTS"""
    db.run(dropAll)
  }

  "Submission Irs Paths" must {
    "return a 200" in {
      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/irs") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        val irs = responseAs[IrsResponse]
        irs.currentPage mustBe 1
        irs.summary.amount mustBe 24
        irs.summary.lars mustBe 2
        irs.msas.length mustBe 2
      }
    }
  }
}
