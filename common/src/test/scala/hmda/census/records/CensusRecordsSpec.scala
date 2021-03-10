package hmda.parser.derivedFields

import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }
import hmda.census.records.CensusRecords._
import hmda.model.census.Census

class CensusRecordsSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("Census Data Should be correctly assigned to tracts and counties") {
    getCensusOnTractandCounty("42079215600", "01001", 2020) mustBe Census(0,2020,42540,"42","079","215600",71700,5422,16.51,1172,2058,49107,81.99,76,false,"Scranton--Wilkes-Barre, PA")
    getCensusOnTractandCounty("", "42079", 2020) mustBe Census(0,0,42540,"42","079","",71700,0,0.0,0,0,0,0.0,0,false,"Scranton--Wilkes-Barre, PA")
    getCensusOnTractandCounty("42079215600", "01001", 2019) mustBe Census(0,2019,42540,"42","079","215600",67000,5422,16.51,1172,2058,49107,81.99,76,false,"Scranton--Wilkes-Barre, PA")
    getCensusOnTractandCounty("", "01001", 2020) mustBe Census(0,0,33860,"01","001","",65700,0,0.0,0,0,0,0.0,0,false,"Montgomery-Selma-Alexander City, AL")
    getCensusOnTractandCounty("", "01001", 2019) mustBe Census(0,0,33860,"01","001","",65900,0,0.0,0,0,0,0.0,0,false,"Montgomery-Selma-Alexander City, AL")

  }
}