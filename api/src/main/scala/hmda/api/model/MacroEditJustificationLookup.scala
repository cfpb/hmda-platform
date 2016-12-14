package hmda.api.model

import hmda.model.ResourceUtils
import hmda.validation.engine.MacroEditJustification

object MacroEditJustificationLookup extends ResourceUtils {
  def apply(): MacroEditJustificationLookup = {
    val lines = resourceLines("/macroEditJustifications.txt")
    val justifications = lines.map { line =>
      val values = line.split('|').map(_.trim)
      val editName = values(0)
      val justId = values(1)
      val value = values(2)
      MacroEditJustificationWithName(editName, MacroEditJustification(justId.toInt, value, false, None))
    }.toSeq
    MacroEditJustificationLookup(justifications)
  }
}

case class MacroEditJustificationLookup(justifications: Seq[MacroEditJustificationWithName])
