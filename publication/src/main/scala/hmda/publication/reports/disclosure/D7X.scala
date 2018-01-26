package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.census.model.{ Tract, TractLookup }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.model.publication.reports.ValueDisposition
import hmda.publication.reports._
import hmda.publication.reports.util.CensusTractUtil._
import hmda.publication.reports.util.DispositionType._
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.ReportsMetaDataLookup

import scala.concurrent.Future

object D71 extends D7X {
  val reportId = "D71"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    (loan.loanType == 2 || loan.loanType == 3 || loan.loanType == 4) &&
      (loan.propertyType == 1 || loan.propertyType == 2) &&
      loan.purpose == 1
  }
}

object D72 extends D7X {
  val reportId = "D72"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 1 && loan.purpose == 1 &&
      (loan.propertyType == 1 || loan.propertyType == 2)
  }
}

object D73 extends D7X {
  val reportId = "D73"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    (loan.propertyType == 1 || loan.propertyType == 2) &&
      loan.purpose == 3
  }
}

object D74 extends D7X {
  val reportId = "D74"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    (loan.propertyType == 1 || loan.propertyType == 2) &&
      loan.purpose == 2
  }
}

object D75 extends D7X {
  val reportId = "D75"
  def filters(lar: LoanApplicationRegister): Boolean = {
    lar.loan.propertyType == 3
  }
}

object D76 extends D7X {
  val reportId = "D76"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.occupancy == 2 &&
      (loan.purpose == 1 || loan.purpose == 2 || loan.purpose == 3) &&
      (loan.propertyType == 1 || loan.propertyType == 2)
  }
}

object D77 extends D7X {
  val reportId = "D77"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.propertyType == 2 &&
      (loan.purpose == 1 || loan.purpose == 2 || loan.purpose == 3)
  }
}

trait D7X extends DisclosureReport {
  val reportId: String
  def filters(lar: LoanApplicationRegister): Boolean

  val dispositions = List(ApplicationReceived, LoansOriginated, ApprovedButNotAccepted,
    ApplicationsDenied, ApplicationsWithdrawn, ClosedForIncompleteness)

  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int,
    institution: Institution
  ): Future[DisclosureReportPayload] = {

    val metaData = ReportsMetaDataLookup.values(reportId)

    val lars = larSource
      .filter(lar => lar.geography.msa.toInt == fipsCode)
      .filter(filters)

    val msa = msaReport(fipsCode.toString).toJsonFormat
    val reportDate = formattedCurrentDate
    val yearF = calculateYear(lars)

    val msaTracts: Set[Tract] = TractLookup.values.filter(_.msa == fipsCode.toString)

    val lowIncomeLars = filterIncomeCharacteristics(lars, 0, 50, msaTracts)
    val moderateIncomeLars = filterIncomeCharacteristics(lars, 50, 80, msaTracts)
    val middleIncomeLars = filterIncomeCharacteristics(lars, 80, 120, msaTracts)
    val upperIncomeLars = filterIncomeCharacteristics(lars, 120, 1000, msaTracts)

    for {
      year <- yearF

      tractMinorityComposition1 <- dispositionsOutput(filterMinorityPopulation(lars, 0, 10, msaTracts))
      tractMinorityComposition2 <- dispositionsOutput(filterMinorityPopulation(lars, 10, 20, msaTracts))
      tractMinorityComposition3 <- dispositionsOutput(filterMinorityPopulation(lars, 20, 50, msaTracts))
      tractMinorityComposition4 <- dispositionsOutput(filterMinorityPopulation(lars, 50, 80, msaTracts))
      tractMinorityComposition5 <- dispositionsOutput(filterMinorityPopulation(lars, 80, 101, msaTracts))

      lowIncome <- dispositionsOutput(lowIncomeLars)
      moderateIncome <- dispositionsOutput(moderateIncomeLars)
      middleIncome <- dispositionsOutput(middleIncomeLars)
      upperIncome <- dispositionsOutput(upperIncomeLars)

      low1 <- dispositionsOutput(filterMinorityPopulation(lowIncomeLars, 0, 10, msaTracts))
      low2 <- dispositionsOutput(filterMinorityPopulation(lowIncomeLars, 10, 20, msaTracts))
      low3 <- dispositionsOutput(filterMinorityPopulation(lowIncomeLars, 20, 50, msaTracts))
      low4 <- dispositionsOutput(filterMinorityPopulation(lowIncomeLars, 50, 80, msaTracts))
      low5 <- dispositionsOutput(filterMinorityPopulation(lowIncomeLars, 80, 101, msaTracts))

      mod1 <- dispositionsOutput(filterMinorityPopulation(moderateIncomeLars, 0, 10, msaTracts))
      mod2 <- dispositionsOutput(filterMinorityPopulation(moderateIncomeLars, 10, 20, msaTracts))
      mod3 <- dispositionsOutput(filterMinorityPopulation(moderateIncomeLars, 20, 50, msaTracts))
      mod4 <- dispositionsOutput(filterMinorityPopulation(moderateIncomeLars, 50, 80, msaTracts))
      mod5 <- dispositionsOutput(filterMinorityPopulation(moderateIncomeLars, 80, 101, msaTracts))

      mid1 <- dispositionsOutput(filterMinorityPopulation(middleIncomeLars, 0, 10, msaTracts))
      mid2 <- dispositionsOutput(filterMinorityPopulation(middleIncomeLars, 10, 20, msaTracts))
      mid3 <- dispositionsOutput(filterMinorityPopulation(middleIncomeLars, 20, 50, msaTracts))
      mid4 <- dispositionsOutput(filterMinorityPopulation(middleIncomeLars, 50, 80, msaTracts))
      mid5 <- dispositionsOutput(filterMinorityPopulation(middleIncomeLars, 80, 101, msaTracts))

      upp1 <- dispositionsOutput(filterMinorityPopulation(upperIncomeLars, 0, 10, msaTracts))
      upp2 <- dispositionsOutput(filterMinorityPopulation(upperIncomeLars, 10, 20, msaTracts))
      upp3 <- dispositionsOutput(filterMinorityPopulation(upperIncomeLars, 20, 50, msaTracts))
      upp4 <- dispositionsOutput(filterMinorityPopulation(upperIncomeLars, 50, 80, msaTracts))
      upp5 <- dispositionsOutput(filterMinorityPopulation(upperIncomeLars, 80, 101, msaTracts))

      smallCty <- dispositionsOutput(filterSmallCounty(lars))
      nonSmall <- dispositionsOutput(filterNotSmallCounty(lars))

      total <- dispositionsOutput(lars)

    } yield {
      val report = s"""
         |{
         |    "respondentId": "${institution.respondentId}",
         |    "institutionName": "${institution.respondent.name}",
         |    "table": "${metaData.reportTable}",
         |    "type": "Disclosure",
         |    "description": "${metaData.description}",
         |    "year": "$year",
         |    "reportDate": "$reportDate",
         |    "msa": $msa,
         |    "censusTractCharacteristics": [
         |        {
         |            "characteristic": "Racial/Ethnic Composition",
         |            "compositions": [
         |                {
         |                    "composition": "Less than 10% minority",
         |                    "dispositions": $tractMinorityComposition1
         |                },
         |                {
         |                    "composition": "10-19% minority",
         |                    "dispositions": $tractMinorityComposition2
         |                },
         |                {
         |                    "composition": "20-49% minority",
         |                    "dispositions": $tractMinorityComposition3
         |                },
         |                {
         |                    "composition": "50-79% minority",
         |                    "dispositions": $tractMinorityComposition4
         |                },
         |                {
         |                    "composition": "80-100% minority",
         |                    "dispositions": $tractMinorityComposition5
         |                }
         |            ]
         |        },
         |        {
         |            "characteristic": "Income Characteristics",
         |            "incomes": [
         |                {
         |                    "income": "Low income",
         |                    "dispositions": $lowIncome
         |                },
         |                {
         |                    "income": "Moderate income",
         |                    "dispositions": $moderateIncome
         |                },
         |                {
         |                    "income": "Middle income",
         |                    "dispositions": $middleIncome
         |                },
         |                {
         |                    "income": "Upper income",
         |                    "dispositions": $upperIncome
         |                }
         |            ]
         |        }
         |    ],
         |    "incomeRaces": [
         |        {
         |            "characteristic": "Income & Racial/Ethnic Composition",
         |            "incomes": [
         |                {
         |                    "income": "Low income",
         |                    "compositions": [
         |                        {
         |                            "composition": "Less than 10% minority",
         |                            "dispositions": $low1
         |                        },
         |                        {
         |                            "composition": "10-19% minority",
         |                            "dispositions": $low2
         |                        },
         |                        {
         |                            "composition": "20-49% minority",
         |                            "dispositions": $low3
         |                        },
         |                        {
         |                            "composition": "50-79% minority",
         |                            "dispositions": $low4
         |                        },
         |                        {
         |                            "composition": "80-100% minority",
         |                            "dispositions": $low5
         |                        }
         |                    ]
         |                },
         |                {
         |                    "income": "Moderate income",
         |                    "compositions": [
         |                        {
         |                            "composition": "Less than 10% minority",
         |                            "dispositions": $mod1
         |                        },
         |                        {
         |                            "composition": "10-19% minority",
         |                            "dispositions": $mod2
         |                        },
         |                        {
         |                            "composition": "20-49% minority",
         |                            "dispositions": $mod3
         |                        },
         |                        {
         |                            "composition": "50-79% minority",
         |                            "dispositions": $mod4
         |                        },
         |                        {
         |                            "composition": "80-100% minority",
         |                            "dispositions": $mod5
         |                        }
         |                    ]
         |                },
         |                {
         |                    "income": "Middle income",
         |                    "compositions": [
         |                        {
         |                            "composition": "Less than 10% minority",
         |                            "dispositions": $mid1
         |                        },
         |                        {
         |                            "composition": "10-19% minority",
         |                            "dispositions": $mid2
         |                        },
         |                        {
         |                            "composition": "20-49% minority",
         |                            "dispositions": $mid3
         |                        },
         |                        {
         |                            "composition": "50-79% minority",
         |                            "dispositions": $mid4
         |                        },
         |                        {
         |                            "composition": "80-100% minority",
         |                            "dispositions": $mid5
         |                        }
         |                    ]
         |                },
         |                {
         |                    "income": "Upper income",
         |                    "compositions": [
         |                        {
         |                            "composition": "Less than 10% minority",
         |                            "dispositions": $upp1
         |                        },
         |                        {
         |                            "composition": "10-19% minority",
         |                            "dispositions": $upp2
         |                        },
         |                        {
         |                            "composition": "20-49% minority",
         |                            "dispositions": $upp3
         |                        },
         |                        {
         |                            "composition": "50-79% minority",
         |                            "dispositions": $upp4
         |                        },
         |                        {
         |                            "composition": "80-100% minority",
         |                            "dispositions": $upp5
         |                        }
         |                    ]
         |                }
         |            ]
         |        }
         |    ],
         |    "types": [
         |        {
         |            "type": "Small County",
         |            "dispositions": $smallCty
         |        },
         |        {
         |            "type": "All Other Tracts",
         |            "dispositions": $nonSmall
         |        }
         |    ],
         |    "total": $total
         |}
     """.stripMargin

      DisclosureReportPayload(metaData.reportTable, fipsCode.toString, report)
    }
  }

  private def dispositionsOutput[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    val calculatedDispositions: Future[List[ValueDisposition]] = Future.sequence(
      dispositions.map(_.calculateValueDisposition(larSource))
    )

    calculatedDispositions.map { list =>
      list.map(disp => disp.toJsonFormat).mkString("[", ",", "]")
    }
  }

}
