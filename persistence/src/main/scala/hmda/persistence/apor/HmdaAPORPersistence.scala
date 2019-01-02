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
import hmda.model.rateSpread.{ RateSpreadError, RateSpreadResponse }
import hmda.parser.apor.APORCsvParser
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.messages.commands.apor.APORCommands.{ CalculateRateSpread, CreateApor, FindApor, ModifyApor }
import hmda.persistence.messages.events.apor.APOREvents.{ AporCreated, AporModified }
import hmda.persistence.model.HmdaPersistentActor

import scala.concurrent.duration._
import scala.util.{ Failure, Success, Try }

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
      case AporModified(newApor, rateType) => rateType match {
        case FixedRate =>
          val date = newApor.rateDate
          val newAporList = newApor :: fixedRate.filter(_.rateDate != date)
          HmdaAPORState(newAporList, variableRate)
        case VariableRate =>
          val date = newApor.rateDate
          val newAporList = newApor :: variableRate.filter(_.rateDate != date)
          HmdaAPORState(fixedRate, newAporList)
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

  override def updateState(event: Event): Unit = {
    state = state.update(event)
  }

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
        log.debug(s"$apor for ${apor.rateDate.toString} already exists, skipping")
        sender() ! AporCreated(apor, rateType)
      } else {
        persist(AporCreated(apor, rateType)) { e =>
          log.debug(s"APOR Persisted: $e")
          updateState(e)
          sender() ! e
        }
      }

    case ModifyApor(newApor, rateType) =>
      val date = newApor.rateDate
      if (state.fixedRate.map(_.rateDate).contains(date) || state.variableRate.map(_.rateDate).contains(date)) {
        persist(AporModified(newApor, rateType)) { e =>
          log.debug(s"APOR Modified: $e")
          updateState(e)
          sender() ! Some(e)
        }
      } else {
        sender() ! None
      }

    case FindApor(rateType, date) =>
      rateType match {
        case FixedRate =>
          sender() ! state.fixedRate.find(apor => apor.rateDate == date)
        case VariableRate =>
          sender() ! state.variableRate.find(apor => apor.rateDate == date)
      }

    case CalculateRateSpread(actionTakenType, loanTerm, amortizationType, apr, lockInDate, reverseMortgage) =>
      val response = getRateSpreadResponse(actionTakenType, loanTerm, amortizationType, apr, lockInDate, reverseMortgage)
      sender() ! response

    case GetState =>
      sender() ! state

    case Shutdown =>
      context stop self

  }

  private def getRateSpreadResponse(actionTakenType: Int, loanTerm: Int, amortizationType: RateType, apr: Double, lockInDate: LocalDate, reverseMortgage: Int): Either[RateSpreadError, RateSpreadResponse] = {
    if (!validLoanTerm(loanTerm)) {
      Left(RateSpreadError(400, "Loan term must be 1-50"))
    } else if (rateSpreadNA(actionTakenType, reverseMortgage)) {
      Right(RateSpreadResponse("NA"))
    } else {
      aporForDateAndLoanTerm(loanTerm, amortizationType, lockInDate) match {
        case Some(apor) => Right(RateSpreadResponse(calculateRateSpread(apr, apor).toString))
        case None => Left(RateSpreadError(404, s"Cannot calculate rate spread; APOR value not found for lock-in date $lockInDate"))
      }
    }
  }

  private def calculateRateSpread(apr: Double, apor: Double): BigDecimal = {
    BigDecimal(apr - apor).setScale(3, BigDecimal.RoundingMode.HALF_UP)
  }

  private def aporForDateAndLoanTerm(loanTerm: Int, amortizationType: RateType, lockInDate: LocalDate): Option[Double] = {
    val aporList = amortizationType match {
      case FixedRate => state.fixedRate
      case VariableRate => state.variableRate
    }

    val aporData = aporList.find { apor =>
      weekNumberForDate(apor.rateDate) == weekNumberForDate(lockInDate) &&
        weekYearForDate(apor.rateDate) == weekYearForDate(lockInDate)
    }

    aporData match {
      case Some(data) =>
        Try(data.values(loanTerm - 1)) match {
          case Success(rate) => Some(rate)
          case Failure(e) => None
        }
      case None => None
    }

  }

  private def validLoanTerm(loanTerm: Int): Boolean = loanTerm >= 1 && loanTerm <= 50

  private def rateSpreadNA(actionTakenType: Int, reverseMortgage: Int): Boolean = {
    (actionTakenType != 1 && actionTakenType != 2 && actionTakenType != 8) ||
      (reverseMortgage != 2)
  }

  private def weekNumberForDate(date: LocalDate): Int = {
    val zoneId = ZoneId.systemDefault()
    val weekField = IsoFields.WEEK_OF_WEEK_BASED_YEAR
    val dateTime = date.atStartOfDay(zoneId)
    dateTime.get(weekField)
  }

  private def weekYearForDate(date: LocalDate): Int = {
    val zoneId = ZoneId.systemDefault()
    val weekField = IsoFields.WEEK_BASED_YEAR
    val dateTime = date.atStartOfDay(zoneId)
    dateTime.get(weekField)
  }

}
