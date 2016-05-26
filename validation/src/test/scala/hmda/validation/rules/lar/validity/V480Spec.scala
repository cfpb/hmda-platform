package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck

class V480Spec extends RaceEditCheckSpec {
  property("Succeeds when race1-race5 all have different numeric values") {
    succeedsWithRace(List("1", "2", "3", "4", "5"))
    succeedsWithRace(List("99", "98", "97", "96", "95"))
  }

  property("Fails when race1 is duplicated in another race field") {
    failsWithRace(List("2", "5", "4", "3", "2"))
    failsWithRace(List("1", "1", "4", "", "2"))
  }
  property("Fails when other race fields are duplicated") {
    failsWithRace(List("4", "5", "6", "7", "7"))
    failsWithRace(List("9", "8", "8", "7", ""))
  }

  property("Blank values in r2-r5 are okay to be duplicated") {
    succeedsWithRace(List("5", "6", "", "", ""))
    succeedsWithRace(List("5", "", "", "8", "9"))
    succeedsWithRace(List("3", "", "", "", ""))
  }

  override def check: EditCheck[LoanApplicationRegister] = V480
}
