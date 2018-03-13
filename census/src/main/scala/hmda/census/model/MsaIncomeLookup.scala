package hmda.census.model

import hmda.model.ResourceUtils
import com.github.tototoshi.csv.CSVParser.parse

// Median income by MSA.
object MsaIncomeLookup extends ResourceUtils {
  val values: Seq[MsaIncome] = {
    val lines = resourceLines("/msa17inc.csv")
    lines.tail.map { line =>
      val values = parse(line, '\\', ',', '"').getOrElse(List())
      val fipsCode = values.head.toInt
      val name = values(1)
      val income = values(2).toInt
      MsaIncome(fipsCode, name, income)
    }.toSeq
  }

  val everyFips: Seq[Int] = values.map(_.fips).filterNot(_ == 99999)
}
