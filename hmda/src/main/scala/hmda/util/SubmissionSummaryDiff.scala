package hmda.util

import hmda.api.http.model.admin.{LeiSubmissionSummaryResponse, SubmissionSummaryResponse, YearlySubmissionSummaryResponse}

import java.io.File
import io.circe.parser._

import scala.collection.immutable.ListMap
import scala.io.Source

object SubmissionSummaryDiff extends App {
  require(args.length == 2, "Usage: [source1.json] [source2.json]")

  def parseFile(path: String): List[LeiSubmissionSummaryResponse] =
    decode[List[LeiSubmissionSummaryResponse]](Source.fromFile(new File(path)).mkString) match {
      case Left(e) => throw e
      case Right(v) => v
    }

  def leiToYearlySummary(s: List[LeiSubmissionSummaryResponse]): Map[String, Map[String, YearlySubmissionSummaryResponse]] =
    ListMap(s.map(ss => ss.lei -> ss.years): _*)

  def toSubmissionsMap(submissions: List[SubmissionSummaryResponse]): Map[String, SubmissionSummaryResponse] =
    ListMap(submissions.map(s => s.submissionId -> s): _*)

  def compareSubmissions(lei: String, year: String, l: Map[String, SubmissionSummaryResponse], r: Map[String, SubmissionSummaryResponse]): Boolean = {
    val allSubmissionIds = l.keySet ++ r.keySet
    allSubmissionIds.forall { sid =>
      (l.get(sid), r.get(sid)) match {
        case (Some(_), None) => println(s"(!) Lei: $lei, year: $year, submission id: $sid, present on the left, but not on the right"); false
        case (None, Some(_)) => println(s"(!) Lei: $lei, year: $year, submission id: $sid, present on the right, but not on the left"); false
        case (Some(ll), Some(rr)) =>
          if (ll == rr) true
          else {
            println(s"(!) Lei: $lei, year: $year, submission id: $sid, differ: $ll vs. $rr")
            false
          }
      }
    }
  }

  def compareYearlySummaries(lei: String, l: Map[String, YearlySubmissionSummaryResponse], r: Map[String, YearlySubmissionSummaryResponse]): Boolean = {
    val allYears = l.keySet ++ r.keySet
    allYears.forall { y =>
      (l.get(y), r.get(y)) match {
        case (Some(_), None) => println(s"(!) Lei: $lei, year: $y, present on the left, but not on the right"); false
        case (None, Some(_)) => println(s"(!) Lei: $lei, year: $y, present on the right, but not on the left"); false
        case (Some(ll), Some(rr)) =>
          if (ll.totalSubmissions != rr.totalSubmissions) {
            println(s"(!) Lei: $lei, year: $y, different total submissions count: ${ll.totalSubmissions} != ${rr.totalSubmissions}")
          }
          compareSubmissions(lei, y, toSubmissionsMap(ll.submissions), toSubmissionsMap(rr.submissions))
        case _ => true // impossible
      }
    }
  }

  def compareSummaries(left: List[LeiSubmissionSummaryResponse], right: List[LeiSubmissionSummaryResponse]): Unit = {
    val l = leiToYearlySummary(left)
    val r = leiToYearlySummary(right)

    val allLeis = l.keySet ++ r.keySet
    allLeis.foreach { lei =>
      (l.get(lei), r.get(lei)) match {
        case (Some(_), None) => println(s"(!) Lei: $lei, present on the left, but not on the right")
        case (None, Some(_)) => println(s"(!) Lei: $lei, present on the right, but not on the left")
        case (Some(ll), Some(rr)) =>
          if (compareYearlySummaries(lei, ll, rr)) {
            println(s"    Lei: $lei, same on both sides")
          }
      }
    }
  }

  compareSummaries(parseFile(args.head), parseFile(args(1)))
}