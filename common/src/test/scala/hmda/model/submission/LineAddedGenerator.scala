package hmda.model.submission

import hmda.messages.submission.HmdaRawDataEvents.LineAdded
import org.scalacheck.Gen

object LineAddedGenerator {
  implicit def lineAddedGen: Gen[LineAdded] = {
    for {
      timestamp <- Gen.choose(1483287071000L, 1514736671000L)
      data <- Gen.alphaStr
    } yield LineAdded(timestamp, data)
  }
}