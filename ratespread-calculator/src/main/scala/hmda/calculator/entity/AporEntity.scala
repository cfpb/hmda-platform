package hmda.calculator.entity

import scala.collection.mutable.Map

//Singleton that will allow the APORScheduler to update from the S3 bucket and accessed by the API endpoints.
object AporEntity {
  var counter = 0
  private var _fixedRateAPORMap = Map[String, APOR]()
  private var _variableRateAPORMap = Map[String, APOR]()

  def fixedRateAPORMap = _fixedRateAPORMap

  def variableRateAPORMap = _variableRateAPORMap

  def AporOperation(apor: APOR, rateType: RateType) {
    if (rateType == FixedRate) {
      if (!_fixedRateAPORMap.contains(apor.rateDate.toString)) {
        _fixedRateAPORMap.put(apor.rateDate.toString, apor)
      } else {
        _fixedRateAPORMap.update(apor.rateDate.toString, apor)
      }
    } else if (rateType == VariableRate) {
      if (!_variableRateAPORMap.contains(apor.rateDate.toString)) {
        _variableRateAPORMap.put(apor.rateDate.toString, apor)
      } else {
        _variableRateAPORMap.update(apor.rateDate.toString, apor)
      }
    }
  }
}
