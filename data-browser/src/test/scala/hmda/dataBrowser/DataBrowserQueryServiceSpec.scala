package hmda.dataBrowser

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl._
import akka.testkit.TestKit
import hmda.dataBrowser.models._
import hmda.dataBrowser.repositories.{ Cache, ModifiedLarRepository }
import hmda.dataBrowser.services.DataBrowserQueryService
import monix.eval.Task
import monix.execution.ExecutionModel
import monix.execution.schedulers.TestScheduler
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpecLike }

class DataBrowserQueryServiceSpec
  extends TestKit(ActorSystem("data-browser-query-service-spec"))
    with WordSpecLike
    with MockFactory
    with ScalaFutures
    with Matchers {
  implicit val mat: Materializer        = Materializer(system)
  implicit val scheduler: TestScheduler = TestScheduler(ExecutionModel.SynchronousExecution)

  val cache: Cache                = mock[Cache]
  val repo: ModifiedLarRepository = mock[ModifiedLarRepository]
  val service                     = new DataBrowserQueryService(repo, cache)

  "DataBrowserQueryService" must {
    "call fetchData without using the cache" in {
      val expected = sampleMlar
      (repo.find _).expects(*).returns(Source.single(expected))
      val source = service.fetchData(Nil)
      val futRes = source.runWith(Sink.head)

      whenReady(futRes) { res =>
        (cache.find _).expects(*).never()
        (cache.findFilers _).expects(*).never()
        res shouldBe expected
      }
    }

    "permuteQueryFields should generate all permutations of the provided QueryFields" in {
      val q1     = QueryField(name = "one", values = List("x", "y"))
      val q2     = QueryField(name = "two", values = List("a", "b"))
      val actual = service.permuteQueryFields(q1 :: q2 :: Nil)
      val expected = List(
        List(
          QueryField("one", List("x")),
          QueryField("two", List("a"))
        ),
        List(
          QueryField("one", List("x")),
          QueryField("two", List("b"))
        ),
        List(
          QueryField("one", List("y")),
          QueryField("two", List("a"))
        ),
        List(
          QueryField("one", List("y")),
          QueryField("two", List("b"))
        )
      )
      actual should contain theSameElementsAs expected
    }

    "fetchAggregate uses the cache to serve results on a cache hit" in {
      val query = QueryField("one", List("a", "b"))

      val e1 = Statistic(1, 1)
      val a1 = Aggregation(e1.count, e1.sum, List(FieldInfo("one", "a")))
      val e2 = Statistic(1, 2)
      val a2 = Aggregation(e2.count, e2.sum, List(FieldInfo("one", "b")))

      // simulate cache hits
      inAnyOrder {
        (cache.find _).expects(List(QueryField("one", List("a")))).returns(Task.now(Some(e1)))
        (cache.find _).expects(List(QueryField("one", List("b")))).returns(Task.now(Some(e2)))
        // you might find this surprising that we expect the repository to be called but we are dealing with an effect
        // system and everything is lazy. Notice if we evaluated this effect, this test would fail
        (repo.findAndAggregate _)
          .expects(*)
          .returns(Task.raiseError(new Exception("You shouldn't be evaluating me on a cache hit")))
          .twice()
      }
      val taskActual: Task[Seq[Aggregation]] = service.fetchAggregate(List(query))
      val futActual                          = taskActual.runToFuture
      scheduler.tick()
      whenReady(futActual)(_ should contain theSameElementsAs List(a1, a2))
    }

    "fetchFilers returns all the institution filers" in {
      val query = List(QueryField("one", List("a")))

      val response = FilerInstitutionResponse(FilerInformation("example", "example", 1, 2018) :: Nil)
      (cache.findFilers _).expects(query).returns(Task.now(None))
      (repo.findFilers _).expects(query).returns(Task.now(response.institutions))
      (cache.updateFilers _).expects(*, *).returns(Task.now(response))

      val taskActual = service.fetchFilers(query)
      val futActual  = taskActual.runToFuture
      scheduler.tick()
      whenReady(futActual)(_ shouldBe response)
    }

    def sampleMlar = ModifiedLarEntity(
      filingYear = 2019,
      lei = "EXAMPLELEI",
      msaMd = 1,
      state = "STATE",
      county = "COUNTY",
      tract = "TRACT",
      conformingLoanLimit = "LOANLIMIT",
      loanProductType = "LOANTYPE",
      dwellingCategory = "DWELLINGCAT",
      ethnicityCategorization = "ETHNICITYCAT",
      raceCategorization = "RACECAT",
      sexCategorization = "SEXCATEG",
      actionTakenType = 1,
      purchaserType = 1,
      preapproval = 1,
      loanType = 1,
      loanPurpose = 1,
      lienStatus = 1,
      reverseMortgage = 1,
      lineOfCredits = 1,
      businessOrCommercial = 100,
      loanAmount = 1,
      loanValueRatio = "2.4%",
      interestRate = "RATESPREAD",
      rateSpread = "1",
      hoepaStatus = 100,
      totalLoanCosts = "100",
      totalPoints = "ORIGINATIONCHARGES",
      originationCharges = "1",
      discountPoints = "lendercredits",
      lenderCredits = "loanterm",
      loanTerm = "penalty",
      paymentPenalty = "ratespread",
      rateSpreadIntro = "1",
      amortization = 1,
      insertOnlyPayment = 1,
      baloonPayment = 1,
      otherAmortization = 1,
      propertyValue = "cm",
      constructionMethod = "1",
      occupancyType = 1,
      homeSecurityPolicy = 1,
      landPropertyInterest = 1,
      totalUnits = "1",
      mfAffordable = "1",
      income = "1",
      debtToIncome = "1",
      creditScoreTypeApplicant = 1,
      creditScoreTypeCoApplicant = 1,
      ethnicityApplicant1 = "1",
      ethnicityApplicant2 = "1",
      ethnicityApplicant3 = "1",
      ethnicityApplicant4 = "1",
      ethnicityApplicant5 = "1",
      ethnicityCoApplicant1 = "1",
      ethnicityCoApplicant2 = "1",
      ethnicityCoApplicant3 = "1",
      ethnicityCoApplicant4 = "1",
      ethnicityCoApplicant5 = "1",
      ethnicityObservedApplicant = "1",
      ethnicityObservedCoApplicant = "1",
      raceApplicant1 = "1",
      raceApplicant2 = "1",
      raceApplicant3 = "1",
      raceApplicant4 = "1",
      raceApplicant5 = "1",
      rateCoApplicant1 = "1",
      rateCoApplicant2 = "1",
      rateCoApplicant3 = "1",
      rateCoApplicant4 = "1",
      rateCoApplicant5 = "2",
      raceObservedApplicant = 2,
      raceObservedCoApplicant = 2,
      sexApplicant = 2,
      sexCoApplicant = 2,
      observedSexApplicant = 2,
      observedSexCoApplicant = 1,
      ageApplicant = "1",
      ageCoApplicant = "1",
      applicantAgeGreaterThan62 = "1",
      coapplicantAgeGreaterThan62 = "1",
      applicationSubmission = 1,
      payable = 1,
      aus1 = "1",
      aus2 = "1",
      aus3 = "1",
      aus4 = "1",
      aus5 = "1",
      denialReason1 = "1",
      denialReason2 = "1",
      denialReason3 = "1",
      denialReason4 = "1",
      population = "1",
      minorityPopulationPercent = "1",
      ffiecMedFamIncome = "1",
      medianIncomePercentage = "1",
      ownerOccupiedUnits = "1",
      oneToFourFamUnits = "1",
      medianAge = 1
    )
  }
}