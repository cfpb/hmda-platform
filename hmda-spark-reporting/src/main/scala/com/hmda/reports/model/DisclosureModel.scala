package com.hmda.reports.model

case class DataMedAge(msa_md: Long,
                      msa_md_name: String,
                      loan_amount: Double,
                      count: Long,
                      dispositionName: String,
                      title: String,
                      median_age_calculated: String = "")
case class AggregateData(tract: String,
                         msa_md: Long,
                         msa_md_name: String,
                         loan_amount: Double,
                         count: Long,
                         dispositionName: String,
                         title: String)
case class Data(tract: String,
                msa_md: Long,
                msa_md_name: String,
                loan_amount: Double,
                count: Long,
                dispositionName: String,
                title: String)
case class Info(dispositionName: String, count: Long, value: Double)
case class InfoMedAge(disposition: String, count: Long, value: Double)
case class Disposition(title: String,
                       values: List[Info],
                       titleForSorting: String)
object Disposition {
  implicit val ordering: Ordering[Disposition] =
    new Ordering[Disposition] {
      override def compare(x: Disposition, y: Disposition): Int = {
        def extractDispositionLetter(full: String): Char =
          full.takeRight(2).head
        val xName = extractDispositionLetter(x.titleForSorting)
        val yName = extractDispositionLetter(y.titleForSorting)
        xName compare yName
      }
    }
}
case class DispositionMedAge(loanCategory: String,
                             dispositions: List[InfoMedAge])

object DispositionMedAge {
  implicit val ordering: Ordering[DispositionMedAge] =
    new Ordering[DispositionMedAge] {
      override def compare(x: DispositionMedAge, y: DispositionMedAge): Int = {
        def extractDispositionLetter(full: String): Char =
          full.takeRight(2).head
        val xName = extractDispositionLetter(x.loanCategory)
        val yName = extractDispositionLetter(y.loanCategory)
        xName compare yName
      }
    }
}

case class Tract(tract: String, dispositions: List[Disposition])
case class Tract2(tract: String, values: List[Info])
case class Msa(id: String, name: String, state: String, stateName: String)
case class Disclosure(msa: Long, tracts: List[Tract])
case class Disclosure2(msa: Long, tracts: List[Tract2])
case class OutDisclosure2(lei: String,
                          institutionName: String,
                          table: String,
                          `type`: String,
                          description: String,
                          year: Int,
                          reportDate: String,
                          msa: Msa,
                          tracts: List[Tract2])
case class OutDisclosure1(lei: String,
                          institutionName: String,
                          table: String,
                          `type`: String,
                          description: String,
                          year: Int,
                          reportDate: String,
                          msa: Msa,
                          tracts: List[Tract])

case class OutAggregate2(table: String,
                         `type`: String,
                         description: String,
                         year: Int,
                         reportDate: String,
                         msa: Msa,
                         tracts: List[Tract2])
case class OutAggregate1(table: String,
                         `type`: String,
                         description: String,
                         year: Int,
                         reportDate: String,
                         msa: Msa,
                         tracts: List[Tract])

case class Institution(lei: String, institutionName: String)
case class StateMapping(county: String = "NA",
                        stateName: String = "NA",
                        stateCode: Int = 0,
                        countyCode: Int = 0)

case class MedianAge(medianAge: String = "",
                     loanCategories: List[DispositionMedAge],
                     ageForSorting: String)
object MedianAge {
  implicit val ordering: Ordering[MedianAge] =
    new Ordering[MedianAge] {
      override def compare(x: MedianAge, y: MedianAge): Int = {
        def extractDispositionLetter(full: String): Char =
          full.takeRight(2).head
        val xName = extractDispositionLetter(x.ageForSorting)
        val yName = extractDispositionLetter(y.ageForSorting)
        xName compare yName
      }
    }
}
case class OutAggregateMedAge(table: String,
                              `type`: String,
                              description: String,
                              year: String,
                              reportDate: String,
                              msa: Msa,
                              characteristic: String,
                              medianAges: List[MedianAge])

case class AgeBuckets(medianAgeRange: String)
case class Grouping(msa_md: Long, msa_md_name: String)
case class GroupingMedAge(msa_md: Long, msa_md_name: String)

case class ReportedInstitutions(msa_md: String,
                                msa_md_name: String,
                                reported_institutions: List[String])
case class OutReportedInstitutions(table: String,
                                   `type`: String,
                                   description: String,
                                   year: String,
                                   reportDate: String,
                                   msa: Msa,
                                   institutions: Set[String])
