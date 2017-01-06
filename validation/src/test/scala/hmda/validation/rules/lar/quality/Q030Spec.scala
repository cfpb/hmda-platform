package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.{ Geography, LarGenerators, LoanApplicationRegister }
import hmda.model.institution.ExternalIdType.UndeterminedExternalId
import hmda.model.institution._
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.{ Failure, Success }
import org.scalacheck.Gen
import org.scalatest.{ Assertion, MustMatchers, WordSpec }
import org.scalatest.prop.{ PropertyChecks, TableFor1 }

import scala.language.implicitConversions

class Q030Spec extends WordSpec with PropertyChecks with LarGenerators with MustMatchers {

  val respondent = Respondent(ExternalId("", UndeterminedExternalId), "some bank", "", "", "")
  val emails = EmailDomains("", "", "")
  val parent = Parent("", 0, "some parent", "", "")
  val topHolder = TopHolder(-1, "", "", "", "")

  val craFI = Institution("123", Agency.CFPB, 2017, InstitutionType.Bank, cra = true, Set(), emails, respondent, hmdaFilerFlag = false, parent, 0, 0, topHolder)
  val nonCraFI = craFI.copy(cra = false)
  val craOrNot = Table("CRA", craFI, nonCraFI)

  "Q030" when {
    "action taken type is 7 or 8 (preapproval)" must {
      val actionTaken = Gen.oneOf(7, 8)
      "pass" in {
        forAll(craOrNot) { implicit fi =>
          forAll(larGen, actionTaken) { (lar, action) =>
            lar.copy(actionTakenType = action).mustPass
          }
        }
      }
    }

    "action taken is 1-6" when {
      val actionTaken = Gen.oneOf(1 to 6)
      val larGen = for {
        lar <- super.larGen
        action <- actionTaken
      } yield lar.copy(actionTakenType = action)

      val smallCountyInMSA: Geography = Geography("10100", "46", "045", "9621.00")
      val smallCountyOutsideMSA: Geography = Geography("NA", "47", "109", "9307.00")
      val largeCountyInMSA = Geography("13820", "01", "117", "0304.08")
      val largeCountyOutsideMSA = Geography("NA", "27", "097", "7804.00")
      val someOtherMSA = "48300"
      val someOtherState = "55"
      val someOtherTract = "7424.02"

      "institution is NOT a CRA reporter" when {
        implicit val fi = nonCraFI
        "all 4 geography fields are NA" must {
          val geo = Geography("NA", "NA", "NA", "NA")
          "pass" in {
            geo.mustPass
          }
        }
        "county is present and small" when {
          "tract is NA" must {
            "pass" in {
              smallCountyInMSA.withoutTract.mustPass
              smallCountyOutsideMSA.withoutTract.mustPass
            }
          }
          "tract is present (and matches)" must {
            "fail" in {
              smallCountyInMSA.mustFail
              smallCountyOutsideMSA.mustFail
            }
          }
        }
      }
      "institution is a CRA reporter" when {
        implicit val fi = craFI
        "all 4 geography fields are NA" must {
          val geo = Geography("NA", "NA", "NA", "NA")
          "fail" in {
            geo.mustFail
          }
        }
        "state or county is NA (no matter what else is true)" must {
          "fail" in {
            forAll(larGen) { (lar) =>
              whenever(lar.geography.state == "NA" || lar.geography.county == "NA") {
                lar.mustFail
              }
            }
          }
        }
        "county is present and small" when {
          "tract is NA" must {
            "pass" in {
              smallCountyInMSA.withoutTract.mustPass
              smallCountyOutsideMSA.withoutTract.mustPass
            }
          }
          "tract is present (and matches)" must {
            "pass" in {
              smallCountyInMSA.mustPass
              smallCountyOutsideMSA.mustPass
            }
          }
        }
      }

      "property MSA/MD is present" when {
        "tract is NA" when {
          "MSA/MD, state and county match" must {
            "pass" in {
              smallCountyInMSA.withoutTract.mustPass(craOrNot)
            }
          }
          "MSA/MD, state and county do not match" must {
            "fail" in {
              smallCountyInMSA.withoutTract.copy(msa = someOtherMSA).mustFail(craOrNot)
            }
          }
          "county is large" must {
            "fail" in {
              largeCountyInMSA.withoutTract.mustFail(craOrNot)
            }
          }
        }
        "tract is present" when {
          "MSA/MD, state, county, and tract match" must {
            "pass" in {
              largeCountyInMSA.mustPass(craOrNot)
            }
          }
          "MSA/MD, state, county, and tract do not match" must {
            "fail" in {
              smallCountyInMSA.copy(tract = someOtherTract).mustFail(craOrNot)
            }
          }
        }
      }

      "property MSA/MD is NA" when {
        "tract is NA" when {
          "state and county match" must {
            "pass" in {
              smallCountyOutsideMSA.withoutTract.mustPass(craOrNot)
            }
          }
          "state and county do not match" must {
            "fail" in {
              smallCountyOutsideMSA.withoutTract.copy(state = someOtherState).mustFail(craOrNot)
            }
          }
          "county is large" must {
            "fail" in {
              largeCountyOutsideMSA.withoutTract.mustFail(craOrNot)
            }
          }
        }
        "tract is present" when {
          "state, county, and tract match" must {
            "pass" in {
              largeCountyOutsideMSA.mustPass(craOrNot)
            }
          }
          "state, county, and tract do not match" must {
            "fail" in {
              smallCountyOutsideMSA.copy(state = someOtherState).mustFail(craOrNot)
              smallCountyOutsideMSA.copy(tract = someOtherTract).mustFail(craOrNot)
            }
          }
        }
      }

      {
        val msaOrNot = Gen.oneOf("13820", "NA")
        val tractOrNot = Gen.oneOf("0304.08", "NA")

        "state is present but county is NA" must {
          forAll(craOrNot) { implicit fi: Institution =>
            s"fail when CRA is ${fi.cra}" in {
              forAll(msaOrNot, tractOrNot) { (msa, tract) =>
                Geography(msa, "01", "NA", tract).mustFail
              }
            }
          }
        }
        "county is present but state is NA" must {
          forAll(craOrNot) { implicit fi: Institution =>
            s"fail when CRA is ${fi.cra}" in {
              forAll(msaOrNot, tractOrNot) { (msa, tract) =>
                Geography(msa, "NA", "117", tract).mustFail
              }
            }
          }
        }
      }

      def mustFail(implicit geo: Geography, fi: Institution): Unit = {
        forAll(larGen, actionTaken) { (lar, action) =>
          lar.copy(actionTakenType = action, geography = geo).mustFail
        }
      }

      implicit class GeoChecker(geo: Geography) {
        def mustFail(implicit fi: Institution) = forAll(larGen) { lar => lar.copy(geography = geo).mustFail }
        def mustPass(implicit fi: Institution) = forAll(larGen) { lar => lar.copy(geography = geo).mustPass }
        def withoutTract = geo.copy(tract = "NA")
        def mustFail(fiGen: TableFor1[Institution]): Assertion = forAll(fiGen) { fi => geo.mustFail(fi) }
        def mustPass(fiGen: TableFor1[Institution]): Assertion = forAll(fiGen) { fi => geo.mustPass(fi) }
      }
    }
  }

  implicit class LarChecker(lar: LoanApplicationRegister) {
    def mustFail(implicit fi: Institution) = check(lar, fi) mustBe a[Failure]
    def mustPass(implicit fi: Institution) = check(lar, fi) mustBe a[Success]
    def check(lar: LoanApplicationRegister, fi: Institution) = Q030.inContext(ValidationContext(Some(fi), None)).apply(lar)
  }

}
