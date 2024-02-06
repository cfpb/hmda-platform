package hmda.publisher.validation

import akka.actor.ActorSystem
import akka.testkit.TestKit
import hmda.publisher.query.component.{ PublisherComponent2018, TransmittalSheetTable, TsRepository }
import hmda.publisher.query.lar.{ LarEntityImpl2018, LarPartFive2018, LarPartFour2018, LarPartOne2018, LarPartSix2018, LarPartThree2018, LarPartTwo2018 }
import hmda.query.ts.TransmittalSheetEntity
import hmda.utils.EmbeddedPostgres
import org.scalatest.concurrent.{ PatienceConfiguration, ScalaFutures }
import org.scalatest.time.{ Millis, Minutes, Span }
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach, FreeSpecLike, Matchers }

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, Future }

class TSLinesCheckTest
  extends TestKit(ActorSystem("PublisherComponent2018Spec"))
    with FreeSpecLike
    with Matchers
    with ScalaFutures
    with EmbeddedPostgres
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with PublisherComponent2018
    with PatienceConfiguration {

  import scala.concurrent.ExecutionContext.Implicits.global

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(2, Minutes), interval = Span(100, Millis))

  val tsRepo2018  = new TsRepository[TransmittalSheetTable](dbConfig, transmittalSheetTable2018)
  val larRepo2018 = new LarRepository2018(dbConfig)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.ready(
      Future.sequence(
        List(
          tsRepo2018.createSchema(),
          larRepo2018.createSchema()
        )
      ),
      30.seconds
    )
  }

  "no data in lar" in {

    val lei1 = "EXAMPLE-LEI"
    val ts1  = TransmittalSheetEntity(lei = lei1, totalLines = 10, submissionId = Some("sub1"))

    val check = new TSLinesCheck(dbConfig, validationTSData2018, validationLarData2018)

    val test = for {
      _      <- tsRepo2018.insert(ts1)
      result <- check.check()
      _ = assert(
        result == Left(
          """Some of the LEIs has different counts of records in Transmittal Sheet table and in LAR table.
            |LEI: EXAMPLE-LEI, submissionId: sub1, countFromTs: 10, countFromLar: null""".stripMargin
        )
      )
    } yield ()

    test.futureValue

  }

  "different data in lar" in {

    val lei1  = "EXAMPLE-LEI"
    val ts1   = TransmittalSheetEntity(lei = lei1, totalLines = 10, submissionId = Some("sub1"))
    val check = new TSLinesCheck(dbConfig, validationTSData2018, validationLarData2018)
    val test = for {
      _      <- tsRepo2018.insert(ts1)
      _      <- larRepo2018.insert(dummyLar2018)
      _      <- larRepo2018.insert(dummyLar2018)
      result <- check.check()
      _ = assert(
        result == Left(
          """Some of the LEIs has different counts of records in Transmittal Sheet table and in LAR table.
            |LEI: EXAMPLE-LEI, submissionId: sub1, countFromTs: 10, countFromLar: 2""".stripMargin
        )
      )
    } yield ()

    test.futureValue

  }

  "matching data in lar - 0" in {

    val lei1 = "EXAMPLE-LEI"
    val ts1  = TransmittalSheetEntity(lei = lei1, totalLines = 0, submissionId = Some("sub1"))

    val check = new TSLinesCheck(dbConfig, validationTSData2018, validationLarData2018)

    val test = for {
      _      <- tsRepo2018.insert(ts1)
      result <- check.check()
      _ = assert(
        result == Right(())
      )
    } yield ()

    test.futureValue

  }

  "matching data in lar - non 0" in {

    val lei1  = "EXAMPLE-LEI"
    val ts1   = TransmittalSheetEntity(lei = lei1, totalLines = 1, submissionId = Some("sub1"))
    val check = new TSLinesCheck(dbConfig, validationTSData2018, validationLarData2018)
    val test = for {
      _      <- tsRepo2018.insert(ts1)
      _      <- larRepo2018.insert(dummyLar2018)
      result <- check.check()
      _ = assert(
        result == Right(())
      )
    } yield ()

    test.futureValue

  }

  def dummyLar2018 = LarEntityImpl2018(
    LarPartOne2018(lei = "EXAMPLE-LEI"),
    LarPartTwo2018(),
    LarPartThree2018(),
    LarPartFour2018(),
    LarPartFive2018(),
    LarPartSix2018()
  )

  override def bootstrapSqlFile: String = ""
}