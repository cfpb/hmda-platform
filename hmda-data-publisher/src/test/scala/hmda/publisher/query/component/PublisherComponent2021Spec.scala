package hmda.publisher.query.component

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.TestKit
import hmda.publisher.query.lar._
import hmda.publisher.query.panel.InstitutionEntity
import hmda.query.ts.TransmittalSheetEntity
import hmda.utils.EmbeddedPostgres
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.time.{Millis, Minutes, Span}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FreeSpecLike, Matchers}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class PublisherComponent2021Spec
  extends TestKit(ActorSystem("PublisherComponent2021Spec"))
    with FreeSpecLike
    with Matchers
    with ScalaFutures
    with EmbeddedPostgres
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with PublisherComponent2021
    with PatienceConfiguration {

  val institutionRepo = new InstitutionRepository2021(dbConfig)
  val tsRepo          = createTransmittalSheetRepository2021(dbConfig, Year2021Period.Whole)
  val larRepo         = createLarRepository2021(dbConfig,Year2021Period.Whole)
  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(2, Minutes), interval = Span(100, Millis))

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.ready(
      Future.sequence(
        List(
          institutionRepo.createSchema(),
          tsRepo.createSchema()
        )
      ),
      30.seconds
    )
  }

  "InstitutionRepository2021 runthrough" in {
    import institutionRepo._
    val data = InstitutionEntity("EXAMPLE-LEI", activityYear = 2020, institutionType = 1, taxId = "ABC", hmdaFiler = true)
    data.toPSV
    val test = for {
      rowsInserted <- insert(data)
      _            = rowsInserted shouldBe 1
      result       <- findByLei("EXAMPLE-LEI")
      _            = result should have length 1
      _            = result.head shouldBe data
      result       <- findActiveFilers(Array.empty)
      _            = result should have length 1
      _            = result.head shouldBe data
      result       <- getAllInstitutions()
      _            = result should have length 1
      _            = result.head shouldBe data
      result       <- count()
      _            = result shouldBe 1
      result       <- deleteByLei("EXAMPLE-LEI")
      _            = result shouldBe 1
    } yield ()

    whenReady(test)(_ => ())
  }

  "TransmittalSheetRepository2021 runthrough" in {
    import tsRepo._
    val data = TransmittalSheetEntity(lei = "EXAMPLE-LEI", institutionName = "EXAMPLE-INSTITUTION", year = 2020)
    data.toPublicPSV
    data.toRegulatorPSV
    val test = for {
      rowsInserted <- insert(data)
      _            = rowsInserted shouldBe 1
      result       <- findByLei("EXAMPLE-LEI")
      _            = result should have length 1
      _            = result.head shouldBe data
      result       <- getAllSheets(Array.empty)
      _            = result should have length 1
      _            = result.head shouldBe data
      // note that we cannot insert quarterly entries through the Slick repository as it is misconfigured
      result <- getAllSheets(Array.empty)
      _      = result should have length 1
      result <- count()
      _      = result shouldBe 1
      result <- deleteByLei("EXAMPLE-LEI")
      _      = result shouldBe 1
    } yield ()

    whenReady(test)(_ => ())
  }

  "LarRepository2021 runthrough" in {
    import larRepo._
    val data = LarEntityImpl2021(
      LarPartOne2021(lei = "EXAMPLE-LEI"),
      LarPartTwo2021(),
      LarPartThree2021(),
      LarPartFour2021(),
      LarPartFive2021(),
      LarPartSix2021(),
      LarPartSeven2021()
    )
    data.toRegulatorPSV
    val test = for {
      _      <- insert(data)
      result <- findByLei("EXAMPLE-LEI")
      _      = result should have length 1
      _      = result.head shouldBe data
      result <- count()
      _      = result shouldBe 1
      result <- Source.fromPublisher(getAllLARs(Array.empty)).runWith(Sink.collection)
      _      = result should have size 1
      _      = result.head shouldBe data
      result <- Source.fromPublisher(getAllLARs(Array.empty)).runWith(Sink.collection)
      _      = result should have size 1
      _      = result.head shouldBe data
      result <- deleteByLei("EXAMPLE-LEI")
      _      = result shouldBe 1
    } yield ()
    whenReady(test)(_ => ())
  }

  // We cannot rely on the larRepo's create and delete schema since it is broken (see PublisherComponent2021#LarRepository2021 for more information)
  override def bootstrapSqlFile: String = "loanapplicationregister2021.sql"
}