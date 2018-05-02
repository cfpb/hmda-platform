package hmda.publication.reports

import hmda.model.publication.reports._
import org.scalacheck.Gen

object ReportGenerators {

  implicit def valueDispositionGen: Gen[ValueDisposition] = {
    for {
      dispositionName <- Gen.alphaStr
      count <- Gen.choose(0, Int.MaxValue)
      value <- Gen.choose(0, Int.MaxValue)
    } yield ValueDisposition(dispositionName, count, value)
  }

}
