package hmda.publisher.checks

import akka.actor.ActorSystem
import akka.testkit.TestKit
import hmda.publisher.query.component.PublisherComponent2018
import hmda.publisher.query.lar._
import hmda.publisher.validation.TSLinesCheck
import hmda.query.ts.TransmittalSheetEntity
import hmda.utils.EmbeddedPostgres
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.time.{Millis, Minutes, Span}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FreeSpecLike, Matchers}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

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

  val check = new TSLinesCheck(dbConfig)

  val tsRepo2018   = new check.TransmittalSheetRepository2018(dbConfig)
  val larRepo2018  = new check.LarRepository2018(dbConfig)
  val mlarRepo2018 = new check.ModifiedLarRepository2018(dbConfig)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.ready(
      Future.sequence(
        List(
          tsRepo2018.createSchema(),
          larRepo2018.createSchema(),
          mlarRepo2018.createSchema()
        )
      ),
      30.seconds
    )
  }

  "2018 check - no data in lar" in {

    val lei1 = "EXAMPLE-LEI"
    val ts1  = TransmittalSheetEntity(lei = lei1, totalLines = 10, submissionId = Some("sub1"))

    val test = for {
      _      <- tsRepo2018.insert(ts1)
      result <- check.check("2018")
      _ = assert(
        result == Left(
          """Error in data publishing for year 2018. Some of the LEIs has different counts of records in Transmittal Sheet table and in LAR table.
            |LEI: EXAMPLE-LEI, submissionId: sub1, countFromTs: 10, countFromLar: null, larTableName: loanapplicationregister2018
            |LEI: EXAMPLE-LEI, submissionId: sub1, countFromTs: 10, countFromLar: null, larTableName: modifiedlar2018"""".stripMargin
        )
      )
    } yield ()

    whenReady(test)(_ => ())

  }

  "2018 check - different data in lar" in {

    val lei1 = "EXAMPLE-LEI"
    val ts1  = TransmittalSheetEntity(lei = lei1, totalLines = 10, submissionId = Some("sub1"))
    val test = for {
      _      <- tsRepo2018.insert(ts1)
      _      <- larRepo2018.insert(dummyLar2018)
      _      <- larRepo2018.insert(dummyLar2018)
      _      <- mlarRepo2018.insert(dummyMLar2018)
      result <- check.check("2018")
      _ = assert(
        result == Left(
          """Error in data publishing for year 2018. Some of the LEIs has different counts of records in Transmittal Sheet table and in LAR table.
            |LEI: EXAMPLE-LEI, submissionId: sub1, countFromTs: 10, countFromLar: 2, larTableName: loanapplicationregister2018
            |LEI: EXAMPLE-LEI, submissionId: sub1, countFromTs: 10, countFromLar: 1, larTableName: modifiedlar2018"""".stripMargin
        )
      )
    } yield ()

    whenReady(test)(_ => ())

  }

  def dummyLar2018 = LarEntityImpl2018(
    LarPartOne2018(lei = "EXAMPLE-LEI"),
    LarPartTwo2018(),
    LarPartThree2018(),
    LarPartFour2018(),
    LarPartFive2018(),
    LarPartSix2018()
  )

  def dummyMLar2018 = ModifiedLarEntityImpl(
    ModifiedLarPartOne(filingYear = Some(2018), lei = "EXAMPLE-LEI"),
    ModifiedLarPartTwo(),
    ModifiedLarPartThree(),
    ModifiedLarPartFour(),
    ModifiedLarPartFive(),
    ModifiedLarPartSix()
  )
  override def bootstrapSqlFile: String = ""
}