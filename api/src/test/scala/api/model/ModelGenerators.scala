package hmda.api.model

import java.util.Calendar
import org.scalacheck.{ Gen, Arbitrary }

trait ModelGenerators {

  implicit def statusGen: Gen[Status] = {
    for {
      status <- Gen.oneOf("OK", "SERVICE_UNAVAILABLE")
      service = "hmda-api"
      time = Calendar.getInstance().getTime().toString
      host = "localhost"
    } yield Status(status, service, time, host)
  }

}
