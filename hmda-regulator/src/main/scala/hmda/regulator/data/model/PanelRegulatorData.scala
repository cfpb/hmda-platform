package hmda.regulator.data.model

import hmda.model.filing.PipeDelimited
import hmda.regulator.data.RegulatorDataEntity

class PanelRegulatorData extends RegulatorDataEntity with PipeDelimited {

  override def toCSV: String = {

    s"testing|1|32|3"
  }

}
