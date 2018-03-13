package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.census.model.{ Tract, TractLookup }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.EthnicityEnum._
import hmda.model.publication.reports.GenderEnum.{ Female, GenderNotAvailable, JointGender, Male }
import hmda.model.publication.reports.ApplicantIncomeEnum._
import hmda.model.publication.reports.MinorityStatusEnum._
import hmda.model.publication.reports.RaceEnum._
import hmda.model.publication.reports.ReportTypeEnum.Aggregate
import hmda.publication.reports.util.CensusTractUtil._
import hmda.publication.reports.{ AS, EC, MAT }
import hmda.publication.reports.util.EthnicityUtil.filterEthnicity
import hmda.publication.reports.util.GenderUtil.filterGender
import hmda.publication.reports.util.MinorityStatusUtil.filterMinorityStatus
import hmda.publication.reports.util.PricingDataUtil._
import hmda.publication.reports.util.RaceUtil.filterRace
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.ReportsMetaDataLookup

import scala.concurrent.Future

object A11_1 extends A11X {
  val reportId = "A11-1"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 2 && loan.purpose == 1 && lar.lienStatus == 1 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

object A11_2 extends A11X {
  val reportId = "A11-2"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 3 && loan.purpose == 1 && lar.lienStatus == 1 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

object A11_3 extends A11X {
  val reportId = "A11-3"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 1 && loan.purpose == 1 && lar.lienStatus == 1 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

object A11_4 extends A11X {
  val reportId = "A11-4"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 1 && loan.purpose == 1 && lar.lienStatus == 2 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

object A11_5 extends A11X {
  val reportId = "A11-5"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 2 && loan.purpose == 3 && lar.lienStatus == 1 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

object A11_6 extends A11X {
  val reportId = "A11-6"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 3 && loan.purpose == 3 && lar.lienStatus == 1 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

object A11_7 extends A11X {
  val reportId = "A11-7"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 1 && loan.purpose == 3 && lar.lienStatus == 1 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

object A11_8 extends A11X {
  val reportId = "A11-8"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 1 && loan.purpose == 3 && lar.lienStatus == 2 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

object A11_9 extends A11X {
  val reportId = "A11-9"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 1 && loan.purpose == 2 && lar.lienStatus == 1 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

object A11_10 extends A11X {
  val reportId = "A11-10"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 1 && loan.purpose == 2 && lar.lienStatus == 2 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

object N11_1 extends A11X {
  val reportId = "N11-1"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 2 && loan.purpose == 1 && lar.lienStatus == 1 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

object N11_2 extends A11X {
  val reportId = "N11-2"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 3 && loan.purpose == 1 && lar.lienStatus == 1 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

object N11_3 extends A11X {
  val reportId = "N11-3"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 1 && loan.purpose == 1 && lar.lienStatus == 1 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

object N11_4 extends A11X {
  val reportId = "N11-4"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 1 && loan.purpose == 1 && lar.lienStatus == 2 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

object N11_5 extends A11X {
  val reportId = "N11-5"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 2 && loan.purpose == 3 && lar.lienStatus == 1 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

object N11_6 extends A11X {
  val reportId = "N11-6"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 3 && loan.purpose == 3 && lar.lienStatus == 1 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

object N11_7 extends A11X {
  val reportId = "N11-7"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 1 && loan.purpose == 3 && lar.lienStatus == 1 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

object N11_8 extends A11X {
  val reportId = "N11-8"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 1 && loan.purpose == 3 && lar.lienStatus == 2 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

object N11_9 extends A11X {
  val reportId = "N11-9"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 1 && loan.purpose == 2 && lar.lienStatus == 1 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

object N11_10 extends A11X {
  val reportId = "N11-10"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 1 && loan.purpose == 2 && lar.lienStatus == 2 &&
      loan.propertyType == 1 && loan.occupancy == 1
  }
}

trait A11X extends AggregateReport {
  val reportId: String
  def filters(lar: LoanApplicationRegister): Boolean

  def geoFilter(fips: Int)(lar: LoanApplicationRegister): Boolean =
    lar.geography.msa != "NA" &&
      lar.geography.msa.toInt == fips

  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int
  ): Future[AggregateReportPayload] = {

    val metaData = ReportsMetaDataLookup.values(reportId)

    val lars =
      if (metaData.reportType == Aggregate) larSource.filter(filters).filter(geoFilter(fipsCode))
      else larSource.filter(filters)

    val larsForIncomeCalculation = lars.filter(lar => lar.applicant.income != "NA" && lar.geography.msa != "NA")
    val incomeIntervals =
      if (metaData.reportType == Aggregate) larsByIncomeInterval(larsForIncomeCalculation, calculateMedianIncomeIntervals(fipsCode))
      else nationalLarsByIncomeInterval(larsForIncomeCalculation)

    val msa: String = if (metaData.reportType == Aggregate) s""""msa": ${msaReport(fipsCode.toString).toJsonFormat},""" else ""
    val reportDate = formattedCurrentDate
    val yearF = calculateYear(lars)

    val msaTracts: Set[Tract] =
      if (metaData.reportType == Aggregate) TractLookup.values.filter(_.msa == fipsCode.toString)
      else TractLookup.values

    for {
      year <- yearF

      e1 <- pricingData(filterEthnicity(lars, HispanicOrLatino))
      e2 <- pricingData(filterEthnicity(lars, NotHispanicOrLatino))
      e3 <- pricingData(filterEthnicity(lars, JointEthnicity))
      e4 <- pricingData(filterEthnicity(lars, NotAvailable))

      r1 <- pricingData(filterRace(lars, AmericanIndianOrAlaskaNative))
      r2 <- pricingData(filterRace(lars, Asian))
      r3 <- pricingData(filterRace(lars, BlackOrAfricanAmerican))
      r4 <- pricingData(filterRace(lars, HawaiianOrPacific))
      r5 <- pricingData(filterRace(lars, White))
      r6 <- pricingData(filterRace(lars, TwoOrMoreMinority))
      r7 <- pricingData(filterRace(lars, JointRace))
      r8 <- pricingData(filterRace(lars, NotProvided))

      m1 <- pricingData(filterMinorityStatus(lars, WhiteNonHispanic))
      m2 <- pricingData(filterMinorityStatus(lars, OtherIncludingHispanic))

      g1 <- pricingData(filterGender(lars, Male))
      g2 <- pricingData(filterGender(lars, Female))
      g3 <- pricingData(filterGender(lars, JointGender))
      g4 <- pricingData(filterGender(lars, GenderNotAvailable))

      i1 <- pricingData(incomeIntervals(LessThan50PercentOfMSAMedian))
      i2 <- pricingData(incomeIntervals(Between50And79PercentOfMSAMedian))
      i3 <- pricingData(incomeIntervals(Between80And99PercentOfMSAMedian))
      i4 <- pricingData(incomeIntervals(Between100And119PercentOfMSAMedian))
      i5 <- pricingData(incomeIntervals(GreaterThan120PercentOfMSAMedian))
      i6 <- pricingData(lars.filter(lar => lar.applicant.income == "NA"))

      tractMinorityComposition1 <- pricingData(filterMinorityPopulation(lars, 0, 10, msaTracts))
      tractMinorityComposition2 <- pricingData(filterMinorityPopulation(lars, 10, 20, msaTracts))
      tractMinorityComposition3 <- pricingData(filterMinorityPopulation(lars, 20, 50, msaTracts))
      tractMinorityComposition4 <- pricingData(filterMinorityPopulation(lars, 50, 80, msaTracts))
      tractMinorityComposition5 <- pricingData(filterMinorityPopulation(lars, 80, 101, msaTracts))

      tractIncome1 <- pricingData(filterIncomeCharacteristics(lars, 0, 50, msaTracts))
      tractIncome2 <- pricingData(filterIncomeCharacteristics(lars, 50, 80, msaTracts))
      tractIncome3 <- pricingData(filterIncomeCharacteristics(lars, 80, 120, msaTracts))
      tractIncome4 <- pricingData(filterIncomeCharacteristics(lars, 120, 1000, msaTracts))

    } yield {
      val report = s"""
                      |{
                      |    "table": "${metaData.reportTable}",
                      |    "type": "${metaData.reportType}",
                      |    "description": "${metaData.description}",
                      |    "year": "$year",
                      |    "reportDate": "$reportDate",
                      |    $msa
                      |    "borrowerCharacteristics": [
                      |        {
                      |            "characteristic": "Race",
                      |            "races": [
                      |                {
                      |                    "race": "American Indian/Alaska Native",
                      |                    "pricingInformation": $r1
                      |                },
                      |                {
                      |                    "race": "Asian",
                      |                    "pricingInformation": $r2
                      |                },
                      |                {
                      |                    "race": "Black or African American",
                      |                    "pricingInformation": $r3
                      |                },
                      |                {
                      |                    "race": "Native Hawaiian or Other Pacific Islander",
                      |                    "pricingInformation": $r4
                      |                },
                      |                {
                      |                    "race": "White",
                      |                    "pricingInformation": $r5
                      |                },
                      |                {
                      |                    "race": "2 or more minority races",
                      |                    "pricingInformation": $r6
                      |                },
                      |                {
                      |                    "race": "Joint (White/Minority Race)",
                      |                    "pricingInformation": $r7
                      |                },
                      |                {
                      |                    "race": "Race Not Available",
                      |                    "pricingInformation": $r8
                      |                }
                      |            ]
                      |        },
                      |        {
                      |            "characteristic": "Ethnicity",
                      |            "ethnicities": [
                      |                {
                      |                    "ethnicity": "Hispanic or Latino",
                      |                    "pricingInformation": $e1
                      |                },
                      |                {
                      |                    "ethnicity": "Not Hispanic or Latino",
                      |                    "pricingInformation": $e2
                      |                },
                      |                {
                      |                    "ethnicity": "Joint (Hispanic or Latino/Not Hispanic or Latino)",
                      |                    "pricingInformation": $e3
                      |                },
                      |                {
                      |                    "ethnicity": "Ethnicity Not Available",
                      |                    "pricingInformation": $e4
                      |                }
                      |            ]
                      |        },
                      |        {
                      |            "characteristic": "Minority Status",
                      |            "minorityStatuses": [
                      |                {
                      |                    "minorityStatus": "White Non-Hispanic",
                      |                    "pricingInformation": $m1
                      |                },
                      |                {
                      |                    "minorityStatus": "Others, Including Hispanic",
                      |                    "pricingInformation": $m2
                      |                }
                      |            ]
                      |        },
                      |        {
                      |            "characteristic": "Income",
                      |            "incomes": [
                      |                {
                      |                    "income": "Less than 50% of MSA/MD median",
                      |                    "pricingInformation": $i1
                      |                },
                      |                {
                      |                    "income": "50-79% of MSA/MD median",
                      |                    "pricingInformation": $i2
                      |                },
                      |                {
                      |                    "income": "80-99% of MSA/MD median",
                      |                    "pricingInformation": $i3
                      |                },
                      |                {
                      |                    "income": "100-119% of MSA/MD median",
                      |                    "pricingInformation": $i4
                      |                },
                      |                {
                      |                    "income": "120% or more of MSA/MD median",
                      |                    "pricingInformation": $i5
                      |                },
                      |                {
                      |                    "income": "Income Not Available",
                      |                    "pricingInformation": $i6
                      |                }
                      |            ]
                      |        },
                      |        {
                      |            "characteristic": "Gender",
                      |            "genders": [
                      |                {
                      |                    "gender": "Male",
                      |                    "pricingInformation": $g1
                      |                },
                      |                {
                      |                    "gender": "Female",
                      |                    "pricingInformation": $g2
                      |                },
                      |                {
                      |                    "gender": "Joint (Male/Female)",
                      |                    "pricingInformation": $g3
                      |                },
                      |                {
                      |                    "gender": "Gender Not Available",
                      |                    "pricingInformation": $g4
                      |                }
                      |            ]
                      |        }
                      |    ],
                      |    "censusTractCharacteristics": [
                      |        {
                      |            "characteristic": "Racial/Ethnic Composition",
                      |            "compositions": [
                      |                {
                      |                    "composition": "Less than 10% minority",
                      |                    "pricingInformation": $tractMinorityComposition1
                      |                },
                      |                {
                      |                    "composition": "10-19% minority",
                      |                    "pricingInformation": $tractMinorityComposition2
                      |                },
                      |                {
                      |                    "composition": "20-49% minority",
                      |                    "pricingInformation": $tractMinorityComposition3
                      |                },
                      |                {
                      |                    "composition": "50-79% minority",
                      |                    "pricingInformation": $tractMinorityComposition4
                      |                },
                      |                {
                      |                    "composition": "80-100% minority",
                      |                    "pricingInformation": $tractMinorityComposition5
                      |                }
                      |            ]
                      |        },
                      |        {
                      |            "characteristic": "Income Characteristics",
                      |            "incomes": [
                      |                {
                      |                    "income": "Low income",
                      |                    "pricingInformation": $tractIncome1
                      |                },
                      |                {
                      |                    "income": "Moderate income",
                      |                    "pricingInformation": $tractIncome2
                      |                },
                      |                {
                      |                    "income": "Middle income",
                      |                    "pricingInformation": $tractIncome3
                      |                },
                      |                {
                      |                    "income": "Upper income",
                      |                    "pricingInformation": $tractIncome4
                      |                }
                      |            ]
                      |        }
                      |    ]
                      |}
     """.stripMargin

      val fipsString = if (metaData.reportType == Aggregate) fipsCode.toString else "nationwide"

      AggregateReportPayload(metaData.reportTable, fipsString, report)
    }
  }
}
