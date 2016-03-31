package hmda.api.model

import java.time.LocalDate
import hmda.api.model.processing.ProcessingStatus
import org.scalacheck.Gen

import scala.util.Try

trait ModelGenerators {

  implicit def statusGen: Gen[Status] = {
    for {
      status <- Gen.oneOf("OK", "SERVICE_UNAVAILABLE")
      service = "hmda-api"
      time <- genLocalDate
      host = "localhost"
    } yield Status(status, service, time.toString, host)
  }

  implicit def processingStatusGen: Gen[ProcessingStatus] = {
    for {
      code <- Gen.oneOf(1, 2, 3, 5, 7, 9)
      respId <- Gen.alphaStr
      date <- genLocalDate
      rows <- Gen.choose(0, Int.MaxValue)
    } yield ProcessingStatus(code + respId, date.toString, rows)
  }

  implicit def genLocalDate: Gen[LocalDate] = {
    for {
      year <- Gen.choose(1970, 2050)
      month <- Gen.choose(1, 12)
      day <- Gen.choose(1, 31)
    } yield Try(LocalDate.of(year, month, day)).getOrElse(LocalDate.of(1970, 10, 31))
  }

}
