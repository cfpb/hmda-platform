package hmda.analytics.query

import org.scalatest.{ MustMatchers, PropSpec }
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class LarConverterSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

    property("Loan Flag must use overall loan values for appropriate years") {
        forAll(larGen) { lar =>
            val geoLar       = lar.copy(geography = lar.geography.copy(state = "NA", county = "NA"))
            val firstLienLar = geoLar.copy(lienStatus = SecuredByFirstLien)
            val unitsLar     = firstLienLar.copy(property = firstLienLar.property.copy(totalUnits = 1))
            val finalLar     = unitsLar.copy(loan = unitsLar.loan.copy(amount = 721151.00))
            LarConverter(finalLar, 2018).conformingLoanLimit mustBe "NC"
            LarConverter(finalLar, 2019).conformingLoanLimit mustBe "U"
            LarConverter(finalLar, 2020).conformingLoanLimit mustBe "U"
            LarConverter(finalLar, 2021).conformingLoanLimit mustBe "U"
            LarConverter(finalLar, 2022).conformingLoanLimit mustBe "U"
            LarConverter(finalLar, 2023).conformingLoanLimit mustBe "C"
            LarConverter(finalLar, 2024).conformingLoanLimit mustBe "C"
        }
    }

    property("Loan Flag must use county values for appropriate years") {
        forAll(larGen) { lar =>
            val geoLar       = lar.copy(geography = lar.geography.copy(state = "06", county = "06067"))
            val firstLienLar = geoLar.copy(lienStatus = SecuredByFirstLien)
            val unitsLar     = firstLienLar.copy(property = firstLienLar.property.copy(totalUnits = 1))
            val finalLar     = unitsLar.copy(loan = unitsLar.loan.copy(amount = 552001.00))
            LarConverter(finalLar, 2019).conformingLoanLimit mustBe "NC"
            LarConverter(finalLar, 2020).conformingLoanLimit mustBe "C"
            LarConverter(finalLar, 2021).conformingLoanLimit mustBe "C"
            LarConverter(finalLar, 2022).conformingLoanLimit mustBe "C"
            LarConverter(finalLar, 2023).conformingLoanLimit mustBe "C"
            LarConverter(finalLar, 2024).conformingLoanLimit mustBe "C"


        }
    }

    property("Loan Flag must use state loan values for appropriate years") {
        forAll(larGen) { lar =>
            val geoLar       = lar.copy(geography = lar.geography.copy(state = "01", county = "NA"))
            val firstLienLar = geoLar.copy(lienStatus = SecuredByFirstLien)
            val unitsLar     = firstLienLar.copy(property = firstLienLar.property.copy(totalUnits = 1))
            val finalLar     = unitsLar.copy(loan = unitsLar.loan.copy(amount = 510300.00))
            LarConverter(finalLar, 2019).conformingLoanLimit mustBe "U"
            LarConverter(finalLar, 2019).isEmpty mustBe lar.larIdentifier.LEI.isEmpty
            LarConverter(finalLar, 2020).conformingLoanLimit mustBe "C"
            LarConverter(finalLar, 2021).conformingLoanLimit mustBe "C"
            LarConverter(finalLar, 2022).conformingLoanLimit mustBe "C"
            LarConverter(finalLar, 2023).conformingLoanLimit mustBe "C"
            LarConverter(finalLar, 2024).conformingLoanLimit mustBe "C"
        }
    }

    property("be able to convert from a Lar to a Lar2018") {
        forAll(larGen) { lar =>
            val lar2018 = LarConverter2018(lar)
            lar2018.isEmpty mustBe lar.larIdentifier.LEI.isEmpty
            lar2018.lei mustBe lar.larIdentifier.LEI
        }
    }

}