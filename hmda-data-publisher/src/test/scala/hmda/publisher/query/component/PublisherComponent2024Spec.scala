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

class PublisherComponent2024Spec
  extends TestKit(ActorSystem("PublisherComponent2024Spec"))
    with FreeSpecLike
    with Matchers
    with ScalaFutures
    with EmbeddedPostgres
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with PublisherComponent2024
    with PatienceConfiguration {

  val institutionRepo = new InstitutionRepository2024(dbConfig)
  val tsRepo          = createTransmittalSheetRepository2024(dbConfig, Year2024Period.Whole)
  val larRepo         = createLarRepository2024(dbConfig,Year2024Period.Whole)
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

  "InstitutionRepository2024 runthrough" in {
    import institutionRepo._
    val data = InstitutionEntity("EXAMPLE-LEI", activityYear = 2024, institutionType = 1, taxId = "ABC", hmdaFiler = true)
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

  "TransmittalSheetRepository2024 runthrough" in {
    import tsRepo._
    val data = TransmittalSheetEntity(lei = "EXAMPLE-LEI", institutionName = "EXAMPLE-INSTITUTION", year = 2024)
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

  "LarRepository2024 runthrough" in {
    import larRepo._
    val data = LarEntityImpl2024(
      LarPartOne2024(lei = "EXAMPLE-LEI"),
      LarPartTwo2024(),
      LarPartThree2024(),
      LarPartFour2024(),
      LarPartFive2024(),
      LarPartSix2024(),
      LarPartSeven2024()
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
  
      override def bootstrapSqlFile: String = "loanapplicationregister2024.sql"
    }