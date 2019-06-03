package com.hmda.reports.model

case class DataRaceEthnicity(msa_md: Long,
                             msa_md_name: String,
                             title: String,
                             race: String,
                             sex: String,
                             ethnicity: String,
                             loan_amount: Double,
                             count: Long)

case class DispositionRaceEthnicity(disposition: String,
                                    count: Long,
                                    value: Double,
                                    dispositionForSorting: String)
object DispositionRaceEthnicity {
  implicit val ordering: Ordering[DispositionRaceEthnicity] =
    new Ordering[DispositionRaceEthnicity] {
      override def compare(x: DispositionRaceEthnicity,
                           y: DispositionRaceEthnicity): Int = {
        def extractDispositionLetter(full: String): Char =
          full.takeRight(2).head
        val xName = extractDispositionLetter(x.dispositionForSorting)
        val yName = extractDispositionLetter(y.dispositionForSorting)
        xName compare yName
      }
    }
}
case class Gender(gender: String,
                  dispositions: List[DispositionRaceEthnicity],
                  genderForSorting: String)
object Gender {
  implicit val ordering: Ordering[Gender] =
    new Ordering[Gender] {
      override def compare(x: Gender, y: Gender): Int = {
        def extractDispositionLetter(full: String): Char =
          full.takeRight(2).head
        val xName = extractDispositionLetter(x.genderForSorting)
        val yName = extractDispositionLetter(y.genderForSorting)
        xName compare yName
      }
    }
}
case class Ethnicity(ethnicityName: String,
                     dispositions: List[DispositionRaceEthnicity],
                     gender: List[Gender],
                     ethnicityNameForSorting: String)
object Ethnicity {
  implicit val ordering: Ordering[Ethnicity] =
    new Ordering[Ethnicity] {
      override def compare(x: Ethnicity, y: Ethnicity): Int = {
        def extractDispositionLetter(full: String): Char =
          full.takeRight(2).head
        val xName = extractDispositionLetter(x.ethnicityNameForSorting)
        val yName = extractDispositionLetter(y.ethnicityNameForSorting)
        xName compare yName
      }
    }
}
case class ReportByEthnicityThenGender(table: String,
                                       `type`: String,
                                       description: String,
                                       year: String,
                                       reportDate: String,
                                       msa: Msa,
                                       ethnicities: List[Ethnicity])
case class Race(race: String,
                dispositions: List[DispositionRaceEthnicity],
                gender: List[Gender],
                raceForSorting: String)
object Race {
  implicit val ordering: Ordering[Race] =
    new Ordering[Race] {
      override def compare(x: Race, y: Race): Int = {
        def extractDispositionLetter(full: String): Char =
          full.takeRight(2).head
        val xName = extractDispositionLetter(x.raceForSorting)
        val yName = extractDispositionLetter(y.raceForSorting)
        xName compare yName
      }
    }
}
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
                          dispositionName: String,
                          title: String)

case class IncomeData(msa_md: Long,
                      msa_md_name: String,
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
                           borrowerCharacteristics: BorrowerCharacteristics,
                           applicantIncomeSorting: String)
object ApplicantIncome {
  implicit val ordering: Ordering[ApplicantIncome] =
    new Ordering[ApplicantIncome] {
      override def compare(x: ApplicantIncome, y: ApplicantIncome): Int = {
        def extractDispositionLetter(full: String): Char =
          full.takeRight(2).head
        val xName = extractDispositionLetter(x.applicantIncomeSorting)
        val yName = extractDispositionLetter(y.applicantIncomeSorting)
        xName compare yName
      }
    }
}
case class BorrowerCharacteristics(race: BorrowerRace,
                                   ethnicity: BorrowerEthnicity)
case class BorrowerRace(characteristic: String, races: List[IncomeRace])
case class BorrowerEthnicity(characteristic: String,
                             ethnicities: List[IncomeEthnicity])
case class IncomeRace(race: String,
                      dispositions: List[IncomeDisposition],
                      raceForSorting: String)
object IncomeRace {
  implicit val ordering: Ordering[IncomeRace] =
    new Ordering[IncomeRace] {
      override def compare(x: IncomeRace, y: IncomeRace): Int = {
        def extractDispositionLetter(full: String): Char =
          full.takeRight(2).head
        val xName = extractDispositionLetter(x.raceForSorting)
        val yName = extractDispositionLetter(y.raceForSorting)
        xName compare yName
      }
    }
}
case class IncomeEthnicity(ethnicityName: String,
                           dispositions: List[IncomeDisposition],
                           ethnicityForSorting: String)
object IncomeEthnicity {
  implicit val ordering: Ordering[IncomeEthnicity] =
    new Ordering[IncomeEthnicity] {
      override def compare(x: IncomeEthnicity, y: IncomeEthnicity): Int = {
        def extractDispositionLetter(full: String): Char =
          full.takeRight(2).head
        val xName = extractDispositionLetter(x.ethnicityForSorting)
        val yName = extractDispositionLetter(y.ethnicityForSorting)
        xName compare yName
      }
    }
}
case class IncomeDisposition(name: String,
                             count: Long,
                             value: Double,
                             nameForSorting: String)

object IncomeDisposition {
  implicit val ordering: Ordering[IncomeDisposition] =
    new Ordering[IncomeDisposition] {
      override def compare(x: IncomeDisposition, y: IncomeDisposition): Int = {
        def extractDispositionLetter(full: String): Char =
          full.takeRight(2).head
        val xName = extractDispositionLetter(x.nameForSorting)
        val yName = extractDispositionLetter(y.nameForSorting)
        xName compare yName
      }
    }
}
