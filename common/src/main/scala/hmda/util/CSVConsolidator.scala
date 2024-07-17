package hmda.util

import cats.implicits._
import com.typesafe.config.ConfigFactory
import hmda.model.filing.submission.SubmissionId

object CSVConsolidator {

   def listDeDupeToString(seqToDeDupe: Seq[String]) = {
    seqToDeDupe.mkString(",").toLowerCase().trim.split("\\s*,\\s*").distinct.mkString(",")
  }
  def listDeDupeToList(seqToDeDupe: Seq[String]) = {
    seqToDeDupe.mkString(",").toLowerCase().trim.split("\\s*,\\s*").distinct.filter(! _.isEmpty).toList
  }

   def stringDeDupeToList(stringToDeDupe: String) = {
    stringToDeDupe.toLowerCase().trim.split("\\s*,\\s*").distinct.filter(! _.isEmpty).toList
  }

   def stringDeDupeToString(stringToDeDupe: String) = {
    stringToDeDupe.toLowerCase().trim.split("\\s*,\\s*").distinct.mkString(",")
  }
}
