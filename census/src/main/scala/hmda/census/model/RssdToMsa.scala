package hmda.census.model

import hmda.model.ResourceUtils

object RssdToMsa extends ResourceUtils {
  val map: Map[String, List[String]] = {
    val mapping: Map[String, List[String]] = Map.empty
    val lines = resourceLines("/branches_msa.csv", "ISO-8859-1")
    lines.drop(1).foldLeft(mapping) { (map, l) =>
      val line = l.split('|').map(_.trim)
      val rssd = line(1)
      val msaAndMd = List(line(17), line(18)).filterNot(_.isEmpty)
      val listOfMsaMd = map.getOrElse(rssd, List.empty) ::: msaAndMd
      map + (rssd -> listOfMsaMd.distinct)
    }
  }
}
