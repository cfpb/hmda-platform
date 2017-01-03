package hmda.parser.fi.panel

import hmda.model.institution.Institution

/**
  * Created by grippinn on 1/3/17.
  */
object PanelCsvParser {
  def apply(s: String): Option[Institution] = {
    val values = (s + " ").split('|').map(_.trim)
      Institution(
        values(0),
        values(1),
        values(2).toInt,
        values(3),
        values(4),
        values(5).toBoolean,
        values(6),
        values(7),
        values(8),
        values(9),
        values(10),
        values(11),
        values(12),
        values(13),
        values(14).toBoolean,
        values(15),
        values(16).toInt,
        values(17),
        values(18),
        values(19),
        values(20).toInt,
        values(21).toInt,
        values(22).toInt,
        values(23),
        values(24),
        values(25),
        values(26)
      )
  }
}
