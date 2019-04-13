package com.hmda.reports.model

case class DataMedAge(msa_md: Long,
                      msa_md_name: String,
                      state: String,
                      loan_amount: Double,
                      count: Long,
                      dispositionName: String,
                      title: String,
                      median_age_calculated: String = "")
case class Data(tract: String,
                msa_md: Long,
                msa_md_name: String,
                state: String,
                loan_amount: Double,
                count: Long,
                dispositionName: String,
                title: String)
case class Info(dispositionName: String, count: Long, value: Double)
case class Disposition(title: String, values: List[Info])
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

case class MedianAge(medianAge: String = "", loanCategories: List[Disposition])
case class OutAggregateMedAge(table: String,
                              `type`: String,
                              description: String,
                              year: String,
                              reportDate: String,
                              msa: Msa,
                              medianAges: List[MedianAge])

case class AgeBuckets(medianAgeRange: String)
case class Grouping(msa_md: Long, msa_md_name: String, state: String)

//Reporting Institutions (Table I)
case class ReportedInstitutions(msa_md: String,
                                msa_md_name: String,
                                state: String,
                                reported_institutions: List[String])
case class OutReportedInstitutions(table: String,
                                   `type`: String,
                                   description: String,
                                   year: String,
                                   reportDate: String,
                                   msa: Msa,
                                   institutions: Set[String])
