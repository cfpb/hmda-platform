package hmda.calculator.apor

import scala.collection.mutable

//Singleton that will allow the APOR rate lists to be updated from the S3 bucket and accessed by the API endpoints.
object AporListEntity {
  private val _fixedRateAPORMap    = mutable.Map[String, APOR]()
  private val _variableRateAPORMap = mutable.Map[String, APOR]()

  def fixedRateAPORMap: mutable.Map[String, APOR] = _fixedRateAPORMap

  def variableRateAPORMap: mutable.Map[String, APOR] = _variableRateAPORMap

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