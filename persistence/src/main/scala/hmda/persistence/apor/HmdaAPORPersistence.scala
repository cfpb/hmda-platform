package hmda.persistence.apor

import java.time.temporal.IsoFields
import java.time.{ LocalDate, ZoneId }

import akka.NotUsed
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.stream.alpakka.s3.{ MemoryBufferType, S3Settings }
import akka.stream.alpakka.s3.scaladsl.S3Client
import akka.stream.scaladsl.{ Flow, Framing, Sink }
import akka.util.{ ByteString, Timeout }
import com.amazonaws.auth.{ AWSStaticCredentialsProvider, BasicAWSCredentials }
import com.typesafe.config.ConfigFactory
import hmda.model.apor.{ APOR, FixedRate, RateType, VariableRate }
import hmda.parser.apor.APORCsvParser
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.messages.commands.apor.APORCommands.{ CalculateRateSpread, CreateApor }
import hmda.persistence.messages.events.apor.APOREvents.AporCreated
import hmda.persistence.model.HmdaPersistentActor
import scala.concurrent.duration._

object HmdaAPORPersistence {
  val name = "hmda-apor-persistence"

  case object LoadAporDataFromS3

  def props(): Props = Props(new HmdaAPORPersistence)
  def createAPORPersistence(system: ActorSystem): ActorRef = {
    system.actorOf(HmdaAPORPersistence.props(), name)
  }

  case class HmdaAPORState(fixedRate: List[APOR] = Nil, variableRate: List[APOR] = Nil) {
    def update(event: Event): HmdaAPORState = event match {
      case AporCreated(apor, rateType) => rateType match {
        case FixedRate => HmdaAPORState(apor :: fixedRate, variableRate)
        case VariableRate => HmdaAPORState(fixedRate, apor :: variableRate)
      }
    }
  }
}

class HmdaAPORPersistence extends HmdaPersistentActor {
  import HmdaAPORPersistence._

  var state = HmdaAPORState()

  override def persistenceId: String = s"$name"

  val config = ConfigFactory.load()
  val accessKeyId = config.getString("hmda.persistence.aws.access-key-id")
  val secretAccess = config.getString("hmda.persistence.aws.secret-access-key")
  val region = config.getString("hmda.persistence.aws.region")
  val bucket = config.getString("hmda.persistence.aws.public-bucket")
  val environment = config.getString("hmda.persistence.aws.environment")
  val fixedRateFileName = config.getString("hmda.apor.fixed.rate.fileName")
  val variableRateFileName = config.getString("hmda.apor.variable.rate.fileName")
  val parallelism = config.getInt("hmda.actor-flow-parallelism")
  val timeoutDuration = config.getInt("hmda.actor.timeout")
  implicit val timeout = Timeout(timeoutDuration.seconds)

  val awsCredentials = new AWSStaticCredentialsProvider(
    new BasicAWSCredentials(accessKeyId, secretAccess)
  )
  val awsSettings = new S3Settings(MemoryBufferType, None, awsCredentials, region, false)
  val s3Client = new S3Client(awsSettings)

  def framing: Flow[ByteString, ByteString, NotUsed] = {
    Framing.delimiter(ByteString("\n"), maximumFrameLength = 65536, allowTruncation = true)
  }

  override def updateState(event: Event): Unit =
    state = state.update(event)

  override def receiveCommand: Receive = {
    case LoadAporDataFromS3 =>
      log.info("Loading APOR data from S3")
      val fixedBucketKey = s"$environment/apor/$fixedRateFileName"
      val s3FixedSource = s3Client.download(bucket, fixedBucketKey)
      s3FixedSource
        .via(framing)
        .drop(1)
        .map(s => s.utf8String)
        .map(s => APORCsvParser(s))
        .mapAsync(parallelism)(apor => self ? CreateApor(apor, FixedRate))
        .runWith(Sink.ignore)

      val variableBucketKey = s"$environment/apor/$variableRateFileName"
      val s3VariableSource = s3Client.download(bucket, variableBucketKey)
      s3VariableSource
        .via(framing)
        .drop(1)
        .map(s => s.utf8String)
        .map(s => APORCsvParser(s))
        .mapAsync(parallelism)(apor => self ? CreateApor(apor, VariableRate))
        .runWith(Sink.ignore)

    case CreateApor(apor, rateType) =>
      if (state.fixedRate.contains(apor) || state.variableRate.contains(apor)) {
        log.debug(s"$apor for ${apor.loanTerm.toString} already exists, skipping")
        sender() ! AporCreated(apor, rateType)
      } else {
        persist(AporCreated(apor, rateType)) { e =>
          log.debug(s"APOR Persisted: $e")
          updateState(e)
          sender() ! e
        }
      }

    case CalculateRateSpread(actionTakenType, amortizationType, rateType, apr, lockinDate, reverseMortgage) =>
      val amortizationTypes = (1 to 50).toList
      val apor = if (List(1, 2, 8).contains(actionTakenType) &&
        amortizationTypes.contains(amortizationType) &&
        reverseMortgage == 2) {
        Some(findApor(amortizationType, rateType, apr, lockinDate))
      } else {
        None
      }
      sender() ! apor

    case GetState =>
      sender() ! state

    case Shutdown =>
      context stop self

  }

  private def findApor(amortizationType: Int, rateType: RateType, apr: Double, lockinDate: LocalDate): Double = {
    rateType match {
      case FixedRate =>
        calculateRateSpread(amortizationType, apr, lockinDate, state.fixedRate)
      case VariableRate =>
        calculateRateSpread(amortizationType, apr, lockinDate, state.variableRate)
    }
  }

  private def calculateRateSpread(amortizationType: Int, apr: Double, lockinDate: LocalDate, aporList: List[APOR]): Double = {
    val zoneId = ZoneId.systemDefault()
    val weekField = IsoFields.WEEK_OF_WEEK_BASED_YEAR
    val dateTime = lockinDate.atStartOfDay(zoneId)
    val week = dateTime.get(weekField)
    val aporObj = aporList.find(apor => apor.loanTerm.get(weekField) == week).getOrElse(APOR())
    val values = aporObj.values
    val apor = if (values.nonEmpty) values(amortizationType) else 0
    apr - apor
  }

}
