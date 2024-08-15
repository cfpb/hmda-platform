//package hmda.publication.lar.publication
//
//import akka.NotUsed
//import akka.actor.ActorSystem
//import akka.actor.typed.{ActorSystem => TypedSystem}
//import akka.actor.typed.scaladsl.AskPattern._
//import akka.actor.typed.scaladsl.Behaviors
//import akka.actor.typed.scaladsl.adapter._
//import akka.actor.typed.{ActorRef, SupervisorStrategy}
//import akka.stream.scaladsl.Source
//import akka.testkit.TestKit
//import akka.util.Timeout
//import com.adobe.testing.s3mock.S3MockApplication
//import hmda.census.records.CensusRecords
//import hmda.messages.submission.HmdaRawDataEvents.LineAdded
//import hmda.model.census.Census
//import hmda.model.filing.lar.{LarGenerators, LoanApplicationRegister}
//import hmda.model.filing.submission.SubmissionId
//import hmda.persistence.util.CassandraUtil
//import hmda.query.repository.ModifiedLarRepository
//import hmda.utils.EmbeddedPostgres
//import hmda.utils.YearUtils.Period
//import io.github.embeddedkafka.EmbeddedKafkaConfig.defaultConfig.{kafkaPort, zooKeeperPort}
//import io.github.embeddedkafka.{EmbeddedK, EmbeddedKafka, EmbeddedKafkaConfig}
//import org.scalacheck.Gen
//import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
//import org.scalatest.time.{Millis, Minutes, Span}
//import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
//import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
//import slick.basic.DatabaseConfig
//import slick.jdbc.JdbcProfile
//
//import scala.annotation.tailrec
//import scala.collection.JavaConverters._
//import scala.collection.mutable
//import scala.concurrent.duration._
//
//class ModifiedLarPublisherSpec
//  extends TestKit(ActorSystem("publisher-spec"))
//    with WordSpecLike
//    with Matchers
//    with PatienceConfiguration
//    with ScalaFutures
//    with ScalaCheckPropertyChecks
//    with BeforeAndAfterAll
//    with EmbeddedPostgres {
//
//  implicit val typedSystem = system.toTyped
//
//  implicit val timeout = Timeout(3.minutes)
//
//  var s3mock: S3MockApplication = _
//  var kafka: EmbeddedK          = _
//
//  implicit val embedKafkaConfig = EmbeddedKafkaConfig(kafkaPort = 9092, zooKeeperPort = 9093)
//
//
//
//
//  override def beforeAll(): Unit = {
//    super.beforeAll()
//    s3mock = S3MockApplication.start(properties.asJava)
//
//    kafka = EmbeddedKafka.start()(embedKafkaConfig)
//
//    CassandraUtil.startEmbeddedCassandra()
//  }
//
//  override def afterAll(): Unit = {
//    super.afterAll()
//    Option(s3mock).foreach(_.stop())
//    Option(kafka).foreach(_.stop(clearLogs = true))
//    CassandraUtil.shutdown()
//  }
//
//  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(2, Minutes), interval = Span(100, Millis))
//
//  "Spawn publisher and upload data to S3 and Postgres" in {
//    @tailrec
//    def generateLarData(gen: Gen[List[LoanApplicationRegister]]): List[LoanApplicationRegister] = {
//      val data = Gen.nonEmptyListOf(LarGenerators.larGen).sample
//      if (data.isEmpty) generateLarData(gen) else data.get
//    }
//
//    val larData                                 = generateLarData(LarGenerators.larNGen(100))
//    val censusTractMap2018: Map[String, Census] = CensusRecords.indexedTract2018
//    val censusTractMap2019: Map[String, Census] = CensusRecords.indexedTract2019
//    val censusTractMap2020: Map[String, Census] = CensusRecords.indexedTract2020
//    val censusTractMap2021: Map[String, Census] = CensusRecords.indexedTract2021
//    val censusTractMap2022: Map[String, Census] = CensusRecords.indexedTract2022
//    val censusTractMap2023: Map[String, Census] = CensusRecords.indexedTract2023
//
//    val customData: TypedSystem[_] => SubmissionId => Source[LineAdded, NotUsed] =
//      _ => _ => Source(larData.zipWithIndex.map { case (lar, timestamp) => LineAdded(timestamp, lar.toCSV) })
//
//    val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("embedded-pg")
//    val repo           = new ModifiedLarRepository(databaseConfig)
//    val publisher = system.spawnAnonymous(
//      Behaviors
//        .supervise(ModifiedLarPublisher.behavior(censusTractMap2018, censusTractMap2019, censusTractMap2020, censusTractMap2021, censusTractMap2022, censusTractMap2023,repo, customData))
//        .onFailure(SupervisorStrategy.stop)
//    )
//    val submissionIdA = SubmissionId("B90YWS6AFX2LGWOXJ1LD", Period(2018, None), sequenceNumber = 1)
//    val submissionIdB = SubmissionId("B90YWS6AFX2LGWOXJ1LD", Period(2019, None), sequenceNumber = 1)
//
//    val resultA = publisher ? ((replyTo: ActorRef[PersistModifiedLarResult]) => PersistToS3AndPostgres(submissionIdA, replyTo))
//    val resultB = publisher ? ((replyTo: ActorRef[PersistModifiedLarResult]) => PersistToS3AndPostgres(submissionIdB, replyTo))
//    resultA.futureValue shouldBe PersistModifiedLarResult(submissionIdA, UploadSucceeded)
//    resultB.futureValue shouldBe PersistModifiedLarResult(submissionIdB, UploadSucceeded)
//  }
//
//  val properties: mutable.Map[String, Object] =
//    mutable // S3 Mock mutates the map so we cannot use an immutable map :(
//      .Map(
//        S3MockApplication.PROP_HTTPS_PORT      -> S3MockApplication.DEFAULT_HTTPS_PORT,
//        S3MockApplication.PROP_HTTP_PORT       -> S3MockApplication.DEFAULT_HTTP_PORT,
//        S3MockApplication.PROP_SILENT          -> true,
//        S3MockApplication.PROP_INITIAL_BUCKETS -> "cfpb-hmda-public-dev"
//      )
//      .map { case (k, v) => (k, v.asInstanceOf[Object]) }
//
//  override def bootstrapSqlFile: String = "modifiedlar.sql"
//}