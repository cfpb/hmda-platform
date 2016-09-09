package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.{ Geography, LoanApplicationRegister }
import hmda.model.institution.{ Agency, Institution, InstitutionType }
import hmda.parser.fi.lar.LarGenerators
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.{ Failure, Success }
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, WordSpec }
import org.scalatest.prop.PropertyChecks

import scala.language.implicitConversions

class Q030WordSpec extends WordSpec with PropertyChecks with LarGenerators with MustMatchers {

  import Q030WordSpec._

  val craFI = Institution("123", "some bank", Set(), Agency.CFPB, InstitutionType.Bank, hasParent = true, cra = true)
  val nonCraFI = Institution("123", "some bank", Set(), Agency.CFPB, InstitutionType.Bank, hasParent = true, cra = false)
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
            // pass
          }
          "tract is present (and matches)" must {
            // fail
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
            // pass
          }
          "tract is present (and matches)" must {
            // pass
          }
        }
      }

      "property MSA/MD is present" when {
        "tract is NA" when {
          "MSA/MD, state and county match" must {
            //pass
          }
          "MSA/MD, state and county do not match" must {
            //fail
            // I'm thinking this will include the case where state is NA, but is it better to make it separate?
            // same question applies to the other "do not match" cases; they should probably be consistent.
          }
          "county is large" must {
            // fail
          }
        }
        "tract is present" when {
          "MSA/MD, state, county, and tract match" must {
            //pass
          }
          "MSA/MD, state, county, and tract do not match" must {
            //fail
          }
        }
      }

      "property MSA/MD is NA" when {
        val msa = "NA"
        "tract is NA" when {
          "state and county match" must {
            //pass
          }
          "state and county do not match" must {
            //fail
          }
          "county is large" must {
            // fail
          }
        }
        "tract is present" when {
          "state, county, and tract match" must {
            //pass
          }
          "state, county, and tract do not match" must {
            //fail
          }
        }
      }

      {
        // these cases use specific values because the edit would otherwise fail for other reasons (value mismatches).
        val msaOrNot = Gen.oneOf(MSA("13820"), MSA("NA"))
        val tractOrNot = Gen.oneOf(Tract("0304.08"), Tract("NA"))

        "state is present but county is NA" must {
          implicit val alabama = State("01")
          implicit val countyNA = County("NA")
          forAll(craOrNot) { implicit fi: Institution =>
            s"fail when CRA is ${fi.cra}" in {
              forAll(msaOrNot) { implicit msa =>
                forAll(tractOrNot) { implicit tract =>
                  mustFail
                }
              }
            }
          }
        }
        "county is present but state is NA" must {
          implicit val stateNA = State("NA")
          implicit val jeffersonCounty = County("117")
          forAll(craOrNot) { implicit fi: Institution =>
            s"fail when CRA is ${fi.cra}" in {
              forAll(msaOrNot) { implicit msa =>
                forAll(tractOrNot) { implicit tract =>
                  mustFail
                }
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
      }
    }
  }

  implicit class LarChecker(lar: LoanApplicationRegister) {
    def mustFail(implicit fi: Institution) = check(lar, fi) mustBe a[Failure]
    def mustPass(implicit fi: Institution) = check(lar, fi) mustBe a[Success]
    def check(lar: LoanApplicationRegister, fi: Institution) = Q030.inContext(ValidationContext(Some(fi))).apply(lar)
  }

}

object Q030WordSpec {

  implicit def msaToString(msa: MSA): String = msa.value
  implicit def stateToString(msa: State): String = msa.value
  implicit def countyToString(msa: County): String = msa.value
  implicit def tractToString(msa: Tract): String = msa.value

  implicit def geography(implicit msa: MSA, state: State, county: County, tract: Tract): Geography = {
    Geography(msa, state, county, tract)
  }
}

case class MSA(value: String) {}
case class State(value: String) {}
case class County(value: String) {}
case class Tract(value: String) {}
