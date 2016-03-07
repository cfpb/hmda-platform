package hmda.validation.rules

import hmda.parser.fi.FIDataDatParser
import hmda.validation.dsl.Success
import org.scalatest.{ FlatSpec, MustMatchers }

class S010Spec extends FlatSpec with MustMatchers {

  //TODO: Change to property based testing once https://github.com/cfpb/hmda-platform/issues/71 is merged
  "Record identifiers" should "have the right values" in {
    import hmda.parser.util.FITestData._

    val fiDataParser = new FIDataDatParser
    val data = fiDataParser.read(fiDAT)

    S010(data) mustBe Success()
  }

}
