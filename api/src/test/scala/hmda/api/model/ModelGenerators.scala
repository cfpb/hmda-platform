package hmda.api.model

import java.util.Calendar

import hmda.api.model.processing.{ Institution, Institutions, ProcessingStatus }
import org.scalacheck.Gen

trait ModelGenerators {

  implicit def statusGen: Gen[Status] = {
    for {
      status <- Gen.oneOf("OK", "SERVICE_UNAVAILABLE")
      service = "hmda-api"
      time = Calendar.getInstance().getTime().toString
      host = "localhost"
    } yield Status(status, service, time, host)
  }

  implicit def processingStatusGen: Gen[ProcessingStatus] = {
    for {
      code <- Gen.choose(0, 12)
      message <- Gen.alphaStr
    } yield ProcessingStatus(code, message)
  }

  implicit def institutionGen: Gen[Institution] = {
    for {
      name <- Gen.alphaStr
      id <- Gen.alphaStr
      period <- Gen.oneOf("2017, 2018", "2019")
      status <- processingStatusGen
      currentSubmission <- Gen.choose(0, 10)
    } yield Institution(name, id, period, status, currentSubmission)
  }

  implicit def institutionsGen: Gen[Institutions] = {
    for {
      institutions <- Gen.listOf(institutionGen)
    } yield Institutions(Set(institutions: _*))
  }

}
