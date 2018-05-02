package hmda.model.institution

import hmda.model.fi._
import org.scalacheck.Gen

object FilingGenerators {

  implicit def filingStatusGen: Gen[FilingStatus] = {
    Gen.oneOf(NotStarted, InProgress, Completed, Cancelled)
  }

  implicit def filingGen: Gen[Filing] = {
    for {
      id <- Gen.alphaStr
      fid <- Gen.alphaStr
      status <- filingStatusGen
      filingRequired <- Gen.oneOf(true, false)
      start <- Gen.choose(1483287071000L, 1514736671000L)
      end <- Gen.choose(1483287071000L, 1514736671000L)
    } yield Filing(id, fid, status, filingRequired, start, end)
  }

  implicit def hmdaFilerGen: Gen[HmdaFiler] = {
    for {
      institutionId <- Gen.alphaStr
      respondentId <- Gen.alphaStr
      period <- Gen.oneOf("2017", "2018")
      name <- Gen.alphaStr
    } yield HmdaFiler(institutionId, respondentId, period, name)
  }

}
