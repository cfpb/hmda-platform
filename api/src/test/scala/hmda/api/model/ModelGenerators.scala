package hmda.api.model

import java.util.Calendar

import hmda.model.fi.{ Active, Inactive, Institution, InstitutionStatus }
import org.scalacheck.{ Arbitrary, Gen }

trait ModelGenerators {

  implicit def statusGen: Gen[Status] = {
    for {
      status <- Gen.oneOf("OK", "SERVICE_UNAVAILABLE")
      service = "hmda-api"
      time = Calendar.getInstance().getTime().toString
      host = "localhost"
    } yield Status(status, service, time, host)
  }

  implicit def institutionStatusGen: Gen[InstitutionStatus] = {
    Gen.oneOf(Active, Inactive)
  }

  implicit def institutionGen: Gen[Institution] = {
    for {
      id <- Gen.alphaStr
      name <- Gen.alphaStr
      status <- institutionStatusGen
    } yield Institution(id, name, status)
  }

}
