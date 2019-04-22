package hmda.publication.lar

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.census._

object IncomeCategorization {
  def assignIncomeCategorization(lar: LoanApplicationRegister,
                                 censusRecords: List[Census]): String = {
    val tract =
      censusRecords.filter(record => record.tract == lar.geography.tract).head
    val medianIncome = tract.medianIncome.toDouble
    val income = lar.income.toDouble

    val fifty = medianIncome * .5
    val eighty = medianIncome * .8
    val oneTwenty = medianIncome * 1.2

    if (income < fifty) {
      "<50%"
    } else if (income > fifty && income < eighty) {
      "50-79%"
    } else if (income > eighty && income < medianIncome) {
      "80-99%"
    } else if (income > medianIncome && income < oneTwenty) {
      "100-119%"
    } else {
      ">120%"
    }

  }
}
