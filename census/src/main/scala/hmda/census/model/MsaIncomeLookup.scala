package hmda.census.model

import hmda.model.ResourceUtils
import com.github.tototoshi.csv.CSVParser.parse

// Median income by MSA.
object MsaIncomeLookup extends ResourceUtils {
  val values: Seq[MsaIncome] = {
    val lines = resourceLines("/msa16inc.csv")
    lines.tail.map { line =>
      val values = parse(line, '\\', ',', '"').getOrElse(List())
      val fipsCode = values(0).toInt
      val name = values(1)
      val income = values(2).toInt
      MsaIncome(fipsCode, name, income)
    }.toSeq
  }
}
