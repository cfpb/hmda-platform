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
    val original = apply().justifications.filter(x => x.edit == editName)
    val originalIndexes: Seq[Int] = original.map(x => x.justification.id)
    val updatedIndexes: Seq[Int] = update.map(x => x.id)
    val indexesInBoth: Seq[Int] = originalIndexes intersect updatedIndexes
    val indexesLeft: Seq[Int] = originalIndexes diff indexesInBoth
    val originalLeft: Seq[MacroEditJustification] = original
      .filter(x => indexesLeft.contains(x.justification.id))
      .map(x => x.justification)
    val merge = update ++ originalLeft
    merge.sortBy(_.id).toSet
  }

  def getJustifications(editName: String): Set[MacroEditJustification] = {
    val filtered = apply().justifications.filter(x => x.edit == editName)
    filtered.map(x => x.justification).toSet
  }

}

case class MacroEditJustificationLookup(justifications: Seq[MacroEditJustificationWithName])
