package hmda.publisher.query.component

sealed trait YearPeriod
object YearPeriod {
  case object Whole extends YearPeriod
  case object Q1 extends YearPeriod
  case object Q2 extends YearPeriod
  case object Q3 extends YearPeriod
}