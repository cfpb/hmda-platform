
package hmda.publication.lar.parser

import hmda.model.filing.lar.LarGenerators._
import org.scalatest.{ Matchers, WordSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ModifiedLarCsvParserSpec extends WordSpec with ScalaCheckPropertyChecks with Matchers {
  "Parses LAR entries" in {
    forAll(larGen) { lar =>
      val mlarFn = ModifiedLarCsvParser(lar.toCSV, _)
      (2018 to 2025).foreach(year => mlarFn(year).lei shouldBe lar.larIdentifier.LEI)
    }
  }

  "isAgeGreaterThan62 renders properly" in {
    forAll(larGen) { lar =>
      val larWithChangedAge = lar.copy(applicant = lar.applicant.copy(age = 8888))
      val mlarFn            = ModifiedLarCsvParser(larWithChangedAge.toCSV, _)
      (2018 to 2025).foreach { year =>
        mlarFn(year).lei shouldBe lar.larIdentifier.LEI
        mlarFn(year).ageGreaterThanOrEqual62 shouldBe "NA"
      }
    }
  }

  "convertAge renders properly" in {
    forAll(larGen) { lar =>
      val ages = List(8888, 9999, 20, 30, 40, 50, 60, 70, 80)
      val mlars = ages
        .map(age => lar.copy(applicant = lar.applicant.copy(age = age)))
        .map(lar => ModifiedLarCsvParser(lar.toCSV, 2018))

      mlars.map(_.age) should contain theSameElementsAs List(
        "8888",
        "9999",
        "<25",
        "25-34",
        "35-44",
        "45-54",
        "55-64",
        "65-74",
        ">74"
      )
    }
  }

  "convertDebtToIncomeRatio renders properly" in {
    forAll(larGen) { lar =>
      val ratios               = List(10, 21, 31, 37, 51, 61)
      val larsWithChangedRatio = ratios.map(ratio => lar.copy(loan = lar.loan.copy(debtToIncomeRatio = ratio.toString)))
      val mlars                = larsWithChangedRatio.map(lar => ModifiedLarCsvParser(lar.toCSV, 2019))
      mlars.map(_.debtToIncomeRatio) should contain atLeastOneElementOf (List(
        "<20%",
        "20%-<30%",
        "30%-<36%",
        "50%-60%",
        ">60%"
      ))
    }
  }

  "convertTotalUnits renders properly" in {
    forAll(larGen) { lar =>
      val larWithChangedAge = lar.copy(property = lar.property.copy(totalUnits = 150))
      val mlarFn            = ModifiedLarCsvParser(larWithChangedAge.toCSV, _)
      (2018 to 2025).foreach { year =>
        mlarFn(year).lei shouldBe lar.larIdentifier.LEI
        mlarFn(year).totalUnits shouldBe ">149"
      }
    }
  }
}