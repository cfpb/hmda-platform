package hmda.parser.fi

import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks

class FIDataCsvParserSpec extends PropSpec with PropertyChecks with MustMatchers with FIDataGenerators {
  property("FI Data must be parsed from CSV")(pending)
}
