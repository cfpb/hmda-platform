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

  /*
  Updates the original justifications configured in macroEditConfigurations.txt with an updated list of configurations,
   filtered for a specific edit
   */
  def updateJustifications(editName: String, update: Seq[MacroEditJustification]): Set[MacroEditJustification] = {
    val original: Seq[MacroEditJustificationWithName] = apply().justifications.filter(x => x.edit == editName)
    val updatedIndexes: Seq[Int] = update.map(x => x.id)
    val originalToUpdate = original.
      filter(x => updatedIndexes.contains(x.justification.id))

    val untouched = original diff originalToUpdate
    val union = untouched.map(x => x.justification) ++ update
    union.sortBy(x => x.id).toSet

  }

  def getJustifications(editName: String): Set[MacroEditJustification] = {
    val filtered = apply().justifications.filter(x => x.edit == editName)
    filtered.map(x => x.justification).toSet
  }

}

case class MacroEditJustificationLookup(justifications: Seq[MacroEditJustificationWithName])
