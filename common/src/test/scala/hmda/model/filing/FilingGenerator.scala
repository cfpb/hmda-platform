package hmda.model.filing

import org.scalacheck.Gen

object FilingGenerator {

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

}
