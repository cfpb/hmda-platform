package hmda.regulator.data

class RegulatorDataEntity() {
  private var _dataType: String = ""

  def dataType: String = _dataType

  def dataType_=(value: String) = {
    _dataType = value
  }

  def toCSV: String = {

    s"testing|1|32|3"
  }

}
