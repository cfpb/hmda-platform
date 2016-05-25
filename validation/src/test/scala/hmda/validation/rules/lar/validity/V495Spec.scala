package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck

class V495Spec extends RaceEditCheckSpec {
  property("Succeeds when coRace1-coRace5 all have different numeric values") {
    succeedsWithCoRace(List("1", "2", "3", "4", "5"))
    succeedsWithCoRace(List("99", "98", "97", "96", "95"))
  }

  property("Fails when coRace1 is duplicated in another coRace field") {
    failsWithCoRace(List("2", "5", "4", "3", "2"))
    failsWithCoRace(List("1", "1", "4", "", "2"))
  }
  property("Fails when other coRace fields are duplicated") {
    failsWithCoRace(List("4", "5", "6", "7", "7"))
    failsWithCoRace(List("9", "8", "8", "7", ""))
  }

  property("Blank values in r2-r5 are okay to be duplicated") {
    succeedsWithCoRace(List("5", "6", "", "", ""))
    succeedsWithCoRace(List("5", "", "", "8", "9"))
    succeedsWithCoRace(List("3", "", "", "", ""))
  }

  override def check: EditCheck[LoanApplicationRegister] = V495
}
