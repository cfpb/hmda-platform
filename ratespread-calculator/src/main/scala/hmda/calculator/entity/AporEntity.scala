package hmda.calculator.entity

import scala.collection.mutable.Map

//Singleton that will allow the APORScheduler to update from the S3 bucket and accessed by the API endpoints.
object AporEntity {
  var counter = 0
  private var fixedRateAPORMap = Map[String, APOR]()
  private var variableRateAPORMap = Map[String, APOR]()

  def AporOperation(apor: APOR, rateType: RateType) {
    if (rateType == FixedRate) {

      if (!fixedRateAPORMap.contains(apor.rateDate.toString)) {
        fixedRateAPORMap.put(apor.rateDate.toString, apor)
      } else {
        fixedRateAPORMap.update(apor.rateDate.toString, apor)
      }
    } else if (rateType == VariableRate) {
      if (!variableRateAPORMap.contains(apor.rateDate.toString)) {
        variableRateAPORMap.put(apor.rateDate.toString, apor)
      } else {
        variableRateAPORMap.update(apor.rateDate.toString, apor)
      }
    }
  }

  def FixedAporCounter: Int = {
    fixedRateAPORMap.keySet.size
  }

  def VariableAporCounter: Int = {
    variableRateAPORMap.keySet.size
  }
}
