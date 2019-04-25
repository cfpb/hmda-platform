package com.hmda.reports.model

case class DataRaceEthnicity(msa_md: Long,
                             msa_md_name: String,
                             state: String,
                             title: String,
                             race: String,
                             sex: String,
                             ethnicity: String,
                             loan_amount: Double,
                             count: Long)

case class DispositionRaceEthnicity(disposition: String,
                                    count: Long,
                                    value: Double)
case class Gender(gender: String, dispositions: List[DispositionRaceEthnicity])
case class Ethnicity(ethnicityName: String,
                     dispositions: List[DispositionRaceEthnicity],
                     gender: List[Gender])
case class ReportByEthnicityThenGender(table: String,
                                       `type`: String,
                                       description: String,
                                       year: String,
                                       reportDate: String,
                                       msa: Msa,
                                       ethnicities: List[Ethnicity])
case class Race(race: String,
                dispositions: List[DispositionRaceEthnicity],
                gender: List[Gender])
case class ReportByRaceThenGender(table: String,
                                  `type`: String,
                                  description: String,
                                  year: String,
                                  reportDate: String,
                                  msa: Msa,
                                  races: List[Race])

case class GroupingRaceEthnicity(msa_md: Long,
                                 msa_md_name: String,
                                 state: String,
                                 dispositionName: String,
                                 title: String)

case class IncomeGrouping(msa_md: Long,
                          msa_md_name: String,
                          state: String,
                          dispositionName: String,
                          title: String)

case class IncomeData(msa_md: Long,
                      msa_md_name: String,
                      state: String,
                      incomeBracket: String,
                      title: String,
                      race: String,
                      ethnicity: String,
                      loan_amount: Double,
                      count: Long)
case class ReportByApplicantIncome(table: String,
                                   `type`: String,
                                   description: String,
                                   year: String,
                                   reportDate: String,
                                   msa: Msa,
                                   applicantIncomes: List[ApplicantIncome])
case class ApplicantIncome(applicantIncome: String,
                           borrowerCharacteristics: BorrowerCharacteristics)
case class BorrowerCharacteristics(race: BorrowerRace,
                                   ethnicity: BorrowerEthnicity)
case class BorrowerRace(characteristic: String, races: List[IncomeRace])
case class BorrowerEthnicity(characteristic: String,
                             ethnicities: List[IncomeEthnicity])
case class IncomeRace(race: String, dispositions: List[IncomeDisposition])
case class IncomeEthnicity(ethnicityName: String,
                           dispositions: List[IncomeDisposition])
case class IncomeDisposition(name: String, count: Long, value: Double)
