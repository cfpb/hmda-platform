package hmda.publisher.query.component

import akka.actor.ActorSystem
import akka.stream.scaladsl.{ Sink, Source }
import akka.testkit.TestKit
import hmda.publisher.query.lar._
import hmda.publisher.query.panel.InstitutionEntity
import hmda.query.ts.TransmittalSheetEntity
import hmda.utils.EmbeddedPostgres
import org.scalatest.concurrent.{ PatienceConfiguration, ScalaFutures }
import org.scalatest.time.{ Millis, Minutes, Span }
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach, FreeSpecLike, Matchers }

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

class PublisherComponent2020Spec
  extends TestKit(ActorSystem("PublisherComponent2020Spec"))
    with FreeSpecLike
    with Matchers
    with ScalaFutures
    with EmbeddedPostgres
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with PublisherComponent2020
    with PatienceConfiguration {

  import dbConfig._
  import dbConfig.profile.api._

  val institutionRepo = new InstitutionRepository2020(dbConfig)
  val tsRepo          = new TransmittalSheetRepository2020(dbConfig)
  val larRepo         = new LarRepository2020(dbConfig)

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

  override def afterEach(): Unit = {
    super.afterEach()

    val truncateAllTables =
      institutionRepo.table.delete >>
        tsRepo.table.delete >>
        larRepo.table.delete

    Await.ready(db.run(truncateAllTables), 30.seconds)
  }

  override def afterAll(): Unit = {
    Await.ready(
      Future.sequence(
        List(
          institutionRepo.dropSchema(),
          tsRepo.dropSchema(),
          larRepo.dropSchema()
        )
      ),
      30.seconds
    )
    super.afterAll()
  }

  "InstitutionRepository2020 runthrough" in {
    import institutionRepo._
    val data = InstitutionEntity("EXAMPLE-LEI", activityYear = 2019, institutionType = 1, taxId = "ABC", hmdaFiler = true)
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

  "TransmittalSheetRepository2020 runthrough" in {
    import tsRepo._
    val data = TransmittalSheetEntity(lei = "EXAMPLE-LEI", institutionName = "EXAMPLE-INSTITUTION", year = 2019)
    val test = for {
      rowsInserted <- insert(data)
      _            = rowsInserted shouldBe 1
      result       <- findByLei("EXAMPLE-LEI")
      _            = result should have length 1
      _            = result.head shouldBe data
      result       <- getAllSheets(Array.empty, includeQuarterly = false)
      _            = result should have length 1
      _            = result.head shouldBe data
      result       <- count()
      _            = result shouldBe 1
      result       <- deleteByLei("EXAMPLE-LEI")
      _            = result shouldBe 1
    } yield ()

    whenReady(test)(_ => ())
  }

  "LarRepository2020 runthrough" in {
    import larRepo._
    val data = LarEntityImpl2020(
      LarPartOne2020(lei = "EXAMPLE-LEI"),
      LarPartTwo2020(),
      LarPartThree2020(),
      LarPartFour2020(),
      LarPartFive2020(),
      LarPartSix2020(),
      LarPartSeven2020()
    )
    val test = for {
      _      <- insert(data)
      result <- findByLei("EXAMPLE-LEI")
      _      = result should have length 1
      _      = result.head shouldBe data
      result <- count()
      _      = result shouldBe 1
      result <- Source.fromPublisher(getAllLARs(Array.empty, includeQuarterly = false)).runWith(Sink.collection)
      _      = result should have length 1
      _      = result.head shouldBe data
      result <- Source.fromPublisher(getAllLARs(Array.empty, includeQuarterly = true)).runWith(Sink.collection)
      _      = result should have length 1
      _      = result.head shouldBe data
      result <- deleteByLei("EXAMPLE-LEI")
      _      = result shouldBe 1
    } yield ()
    whenReady(test)(_ => ())
  }

  override def cleanupAction: DBIO[Int] = DBIO.successful(1)

  // We cannot rely on the larRepo's create and delete schema since it is broken (see PublisherComponent2020#LarRepository2020 for more information)
  override def bootstrapSqlFile: String = "loanapplicationregister2020.sql"
}