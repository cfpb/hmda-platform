package hmda.parser.fi

import hmda.model.fi.FIData
import hmda.model.fi.lar.LarGenerators
import hmda.model.fi.ts.TsGenerators
import org.scalacheck.Gen

trait FIDataGenerators extends TsGenerators with LarGenerators {

  implicit def fiDataGen: Gen[FIData] = {
    for {
      ts <- tsGen
      n <- Gen.choose(1, 1000)
      lars <- Gen.listOfN(n, larGen)
    } yield FIData(ts, lars)
  }

}
