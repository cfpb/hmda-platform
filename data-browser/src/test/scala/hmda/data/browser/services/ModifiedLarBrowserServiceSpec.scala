package hmda.data.browser.services

import java.sql.Timestamp

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import hmda.data.browser.models.{
  ActionTaken,
  Aggregation,
  ModifiedLarEntity,
  MsaMd,
  Race,
  State
}
import hmda.data.browser.repositories.{
  ModifiedLarAggregateCache,
  ModifiedLarRepository,
  Statistic
}
import monix.eval.Task
import monix.execution.Scheduler
import org.scalamock.scalatest.MockFactory
import org.scalatest.{
  BeforeAndAfterAll,
  FunSpec,
  MustMatchers,
  OneInstancePerTest
}

class ModifiedLarBrowserServiceSpec
  extends FunSpec
    with BeforeAndAfterAll
    with MustMatchers
    with OneInstancePerTest
    with MockFactory {
  implicit val system: ActorSystem = ActorSystem("test-system")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val monixScheduler: Scheduler = Scheduler.global
  val cache: ModifiedLarAggregateCache = mock[ModifiedLarAggregateCache]
  val database: ModifiedLarRepository = mock[ModifiedLarRepository]
  val service: ModifiedLarBrowserService =
    new ModifiedLarBrowserService(database, cache)
  import service._

  override def afterAll(): Unit =
    Task.deferFuture(system.terminate()).runSyncUnsafe()

  describe("fetch") {
    val exampleEntity = ModifiedLarEntity(
      id = 1,
      lei = "",
      loanType = 1,
      loanPurpose = 1,
      preapproval = 1,
      constructionMethod = "",
      occupancyType = 1,
      loanAmount = 100.0,
      actionTakenType = 1,
      state = "",
      county = "",
      tract = "",
      ethnicityApplicant1 = "",
      ethnicityApplicant2 = "",
      ethnicityApplicant3 = "",
      ethnicityApplicant4 = "",
      ethnicityApplicant5 = "",
      ethnicityObservedApplicant = 1,
      ethnicityCoApplicant1 = "",
      ethnicityCoApplicant2 = "",
      ethnicityCoApplicant3 = "",
      ethnicityCoApplicant4 = "",
      ethnicityCoApplicant5 = "",
      ethnicityObservedCoApplicant = 1,
      raceApplicant1 = "",
      raceApplicant2 = "",
      raceApplicant3 = "",
      raceApplicant4 = "",
      raceApplicant5 = "",
      rateCoApplicant1 = "",
      rateCoApplicant2 = "",
      rateCoApplicant3 = "",
      rateCoApplicant4 = "",
      rateCoApplicant5 = "",
      raceObservedApplicant = 1,
      raceObservedCoApplicant = 1,
      sexApplicant = 1,
      sexCoApplicant = 1,
      observedSexApplicant = 1,
      observedSexCoApplicant = 1,
      ageApplicant = "",
      applicantAgeGreaterThan62 = "",
      ageCoApplicant = "",
      coapplicantAgeGreaterThan62 = "",
      income = "",
      purchaserType = 1,
      rateSpread = "",
      hoepaStatus = 1,
      lienStatus = 1,
      creditScoreTypeApplicant = 1,
      creditScoreTypeCoApplicant = 1,
      denialReason1 = "",
      denialReason2 = "",
      denialReason3 = "",
      denialReason4 = "",
      totalLoanCosts = "",
      totalPoints = "",
      originationCharges = "",
      discountPoints = "",
      lenderCredits = "",
      interestRate = "",
      paymentPenalty = "",
      debtToIncome = "",
      loanValueRatio = "",
      loanTerm = "",
      rateSpreadIntro = "",
      baloonPayment = 1,
      insertOnlyPayment = 1,
      amortization = 1,
      otherAmortization = 1,
      propertyValue = "",
      homeSecurityPolicy = 1,
      landPropertyInterest = 1,
      totalUnits = "",
      mfAffordable = "",
      applicationSubmission = 1,
      payable = 1,
      aus1 = 1,
      aus2 = 1,
      aus3 = 1,
      aus4 = 1,
      aus5 = 1,
      reverseMortgage = 1,
      lineOfCredits = 1,
      businessOrCommercial = 1,
      population = "",
      minorityPopulationPercent = "",
      ffiecMedFamIncome = "",
      tractToMsamd = "",
      ownerOccupiedUnits = "",
      oneToFourFamUnits = "",
      msaMd = 1,
      loanFlag = "",
      createdAt = Timestamp.valueOf("2019-01-01 09:00:00"),
      submissionId = "",
      msaMdName = "",
      filingYear = 1,
      conformingLoanLimit = "",
      medianAge = 1,
      medianAgeCalculated = "",
      medianIncomePercentage = "",
      raceCategorization = "",
      sexCategorization = "",
      ethnicityCategorization = "",
      uniqId = 1,
      percentMedianMsaIncome = ""
    )

    it("fetches a stream of entities for all queries") {
      // nationwide
      (database
        .find(_: Seq[Int], _: Seq[String]))
        .expects(ActionTaken.values.map(_.value), Race.values.map(_.entryName))
        .returns(Source(List(exampleEntity, exampleEntity)))

      // msa
      (database
        .find(_: Int, _: Seq[Int], _: Seq[String]))
        .expects(*,
          ActionTaken.values.map(_.value),
          Race.values.map(_.entryName))
        .returns(Source(List(exampleEntity, exampleEntity, exampleEntity)))

      // state
      (database
        .find(_: String, _: Seq[Int], _: Seq[String]))
        .expects(*,
          ActionTaken.values.map(_.value),
          Race.values.map(_.entryName))
        .returns(Source(List(exampleEntity)))

      val nationwide = Task
        .deferFuture(
          service.fetchData(Race.values, ActionTaken.values).runWith(Sink.seq))
        .map(actual =>
          actual must contain theSameElementsAs Seq.fill(2)(exampleEntity))

      val msa = Task
        .deferFuture(
          service
            .fetchData(MsaMd(123456), Race.values, ActionTaken.values)
            .runWith(Sink.seq))
        .map(actual =>
          actual must contain theSameElementsAs Seq.fill(3)(exampleEntity))

      val state = Task
        .deferFuture(
          service
            .fetchData(State.CA, Race.values, ActionTaken.values)
            .runWith(Sink.seq))
        .map(actual => actual must contain theSameElementsAs Seq(exampleEntity))

      val test = for {
        _ <- nationwide
        _ <- msa
        _ <- state
      } yield ()

      test.runSyncUnsafe()
    }
  }

  describe("fetchAggregate") {
    val allRaces = Race.values
    val allActions = ActionTaken.values
    val allCombinations = for {
      race <- allRaces
      action <- allActions
    } yield (race, action)

    it("fetches all combinations for all queries with cache hits") {
      val statNationwide = Statistic(count = 10, sum = 100)
      val statMsa = Statistic(count = 12, sum = 800)
      val statState = Statistic(count = 16, sum = 1100)
      val exampleState = State.CA
      val exampleMsa = MsaMd(msaMd = 99999)
      val combinationLength = allCombinations.length

      allCombinations.foreach {
        case (race, action) =>
          // set up the cache to always have a hit
          // nationwide
          (cache
            .find(_: Int, _: String))
            .expects(action.value, race.entryName)
            .returns(Task(Some(statNationwide)))

          // msamd
          (cache
            .find(_: Int, _: Int, _: String))
            .expects(exampleMsa.msaMd, action.value, race.entryName)
            .returns(Task(Some(statMsa)))

          // state
          (cache
            .find(_: String, _: Int, _: String))
            .expects(exampleState.entryName, action.value, race.entryName)
            .returns(Task(Some(statState)))
      }

      // set up the database to fail if it has been called (we have to define this to make ScalaMock happy)
      // nationwide
      (database
        .findAndAggregate(_: Int, _: String))
        .expects(*, *)
        .returns(
          Task.raiseError(new Exception("this should not have been called")))
        .repeated(combinationLength)

      // msamd
      (database
        .findAndAggregate(_: Int, _: Int, _: String))
        .expects(*, *, *)
        .returns(
          Task.raiseError(new Exception("this should not have been called")))
        .repeated(combinationLength)

      // state
      (database
        .findAndAggregate(_: String, _: Int, _: String))
        .expects(*, *, *)
        .returns(
          Task.raiseError(new Exception("this should not have been called")))
        .repeated(combinationLength)

      def expectedResults(stat: Statistic): Seq[Aggregation] =
        allCombinations.map {
          case (race, actionTaken: ActionTaken) =>
            Aggregation(stat.count, stat.sum, race, actionTaken)
        }

      val nationwide = fetchAggregate(allRaces, allActions).map(
        _ must contain theSameElementsAs expectedResults(statNationwide))
      val msa = fetchAggregate(exampleMsa, allRaces, allActions).map(
        _ must contain theSameElementsAs expectedResults(statMsa))
      val state = fetchAggregate(exampleState, allRaces, allActions).map(
        _ must contain theSameElementsAs expectedResults(statState))

      val test = for {
        _ <- nationwide
        _ <- msa
        _ <- state
      } yield ()

      test.runSyncUnsafe()
    }

    it("fetches all combinations for all queries with cache misses") {
      // set up all combinations to have count=10 & sum=100
      val stat = Statistic(count = 10, sum = 100)
      val exampleState = State.AK
      val exampleMsa = MsaMd(msaMd = 981981)
      val combinationLength = allCombinations.length

      // set up the cache to always miss
      (cache
        .find(_: Int, _: String))
        .expects(*, *)
        .returns(Task(None))
        .repeated(combinationLength)

      (cache
        .find(_: Int, _: Int, _: String))
        .expects(*, *, *)
        .returns(Task(None))
        .repeated(combinationLength)

      (cache
        .find(_: String, _: Int, _: String))
        .expects(*, *, *)
        .returns(Task(None))
        .repeated(combinationLength)

      allCombinations.foreach {
        case (race, action) =>
          (database
            .findAndAggregate(_: Int, _: String))
            .expects(action.value, race.entryName)
            .returns(Task(stat))

          (database
            .findAndAggregate(_: Int, _: Int, _: String))
            .expects(exampleMsa.msaMd, action.value, race.entryName)
            .returns(Task(stat))

          (database
            .findAndAggregate(_: String, _: Int, _: String))
            .expects(exampleState.entryName, action.value, race.entryName)
            .returns(Task(stat))

          (cache
            .update(_: Int, _: String, _: Statistic))
            .expects(action.value, race.entryName, stat)
            .returns(Task(stat))

          (cache
            .update(_: Int, _: Int, _: String, _: Statistic))
            .expects(exampleMsa.msaMd, action.value, race.entryName, stat)
            .returns(Task(stat))

          (cache
            .update(_: String, _: Int, _: String, _: Statistic))
            .expects(exampleState.entryName, action.value, race.entryName, stat)
            .returns(Task(stat))
      }

      val expectedResults =
        allCombinations.map {
          case (race, actionTaken: ActionTaken) =>
            Aggregation(stat.count, stat.sum, race, actionTaken)
        }

      val nationwide = fetchAggregate(allRaces, allActions).map(
        _ must contain theSameElementsAs expectedResults)
      val msa = fetchAggregate(exampleMsa, allRaces, allActions).map(
        _ must contain theSameElementsAs expectedResults)
      val state = fetchAggregate(exampleState, allRaces, allActions).map(
        _ must contain theSameElementsAs expectedResults)

      val test = for {
        _ <- nationwide
        _ <- msa
        _ <- state
      } yield ()

      test.runSyncUnsafe()
    }
  }
}