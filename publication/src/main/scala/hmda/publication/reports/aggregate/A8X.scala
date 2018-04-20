package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.PercentageDisposition
import hmda.model.publication.reports.EthnicityEnum._
import hmda.model.publication.reports.GenderEnum._
import hmda.model.publication.reports.ApplicantIncomeEnum._
import hmda.model.publication.reports.MinorityStatusEnum._
import hmda.model.publication.reports.RaceEnum._
import hmda.model.publication.reports.ReportTypeEnum.Aggregate
import hmda.publication.reports._
import hmda.publication.reports.util.DispositionType._
import hmda.publication.reports.util.EthnicityUtil.filterEthnicity
import hmda.publication.reports.util.GenderUtil.filterGender
import hmda.publication.reports.util.MinorityStatusUtil.filterMinorityStatus
import hmda.publication.reports.util.RaceUtil.filterRace
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.ReportsMetaDataLookup

import scala.concurrent.Future

object A81 extends A8X {
  val reportId = "A81"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.loanType == 2 || lar.loan.loanType == 3 || lar.loan.loanType == 4) &&
      (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 1)
  }
}

object A82 extends A8X {
  val reportId = "A82"
  def filters(lar: LoanApplicationRegister): Boolean = {
    lar.loan.loanType == 1 &&
      (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 1)
  }
}

object A83 extends A8X {
  val reportId = "A83"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 3)
  }
}

object A84 extends A8X {
  val reportId = "A84"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 2)
  }
}

object A85 extends A8X {
  val reportId = "A85"
  def filters(lar: LoanApplicationRegister): Boolean = lar.loan.propertyType == 3
}

object A86 extends A8X {
  val reportId = "A86"
  def filters(lar: LoanApplicationRegister): Boolean = {
    lar.loan.occupancy == 2 &&
      (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 1 || lar.loan.purpose == 2 || lar.loan.purpose == 3)
  }
}

object A87 extends A8X {
  val reportId = "A87"
  def filters(lar: LoanApplicationRegister): Boolean = {
    lar.loan.propertyType == 2 &&
      (lar.loan.purpose == 1 || lar.loan.purpose == 2 || lar.loan.purpose == 3)
  }
}

object N81 extends A8X {
  val reportId = "N81"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.loanType == 2 || lar.loan.loanType == 3 || lar.loan.loanType == 4) &&
      (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 1)
  }
}

object N82 extends A8X {
  val reportId = "N82"
  def filters(lar: LoanApplicationRegister): Boolean = {
    lar.loan.loanType == 1 &&
      (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 1)
  }
}

object N83 extends A8X {
  val reportId = "N83"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 3)
  }
}

object N84 extends A8X {
  val reportId = "N84"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 2)
  }
}

object N85 extends A8X {
  val reportId = "N85"
  def filters(lar: LoanApplicationRegister): Boolean = lar.loan.propertyType == 3
}

object N86 extends A8X {
  val reportId = "N86"
  def filters(lar: LoanApplicationRegister): Boolean = {
    lar.loan.occupancy == 2 &&
      (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 1 || lar.loan.purpose == 2 || lar.loan.purpose == 3)
  }
}

object N87 extends A8X {
  val reportId = "N87"
  def filters(lar: LoanApplicationRegister): Boolean = {
    lar.loan.propertyType == 2 &&
      (lar.loan.purpose == 1 || lar.loan.purpose == 2 || lar.loan.purpose == 3)
  }
}

trait A8X extends AggregateReport {
  val reportId: String
  def filters(lar: LoanApplicationRegister): Boolean

  val dispositions = List(DebtToIncomeRatio, EmploymentHistory, CreditHistory,
    Collateral, InsufficientCash, UnverifiableInformation, CreditAppIncomplete,
    MortgageInsuranceDenied, OtherDenialReason)

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

    for {
      year <- yearF

      e1 <- dispositionsOutput(filterEthnicity(lars, HispanicOrLatino))
      e2 <- dispositionsOutput(filterEthnicity(lars, NotHispanicOrLatino))
      e3 <- dispositionsOutput(filterEthnicity(lars, JointEthnicity))
      e4 <- dispositionsOutput(filterEthnicity(lars, NotAvailable))

      r1 <- dispositionsOutput(filterRace(lars, AmericanIndianOrAlaskaNative))
      r2 <- dispositionsOutput(filterRace(lars, Asian))
      r3 <- dispositionsOutput(filterRace(lars, BlackOrAfricanAmerican))
      r4 <- dispositionsOutput(filterRace(lars, HawaiianOrPacific))
      r5 <- dispositionsOutput(filterRace(lars, White))
      r6 <- dispositionsOutput(filterRace(lars, TwoOrMoreMinority))
      r7 <- dispositionsOutput(filterRace(lars, JointRace))
      r8 <- dispositionsOutput(filterRace(lars, NotProvided))

      m1 <- dispositionsOutput(filterMinorityStatus(lars, WhiteNonHispanic))
      m2 <- dispositionsOutput(filterMinorityStatus(lars, OtherIncludingHispanic))

      g1 <- dispositionsOutput(filterGender(lars, Male))
      g2 <- dispositionsOutput(filterGender(lars, Female))
      g3 <- dispositionsOutput(filterGender(lars, JointGender))
      g4 <- dispositionsOutput(filterGender(lars, GenderNotAvailable))

      i1 <- dispositionsOutput(incomeIntervals(LessThan50PercentOfMSAMedian))
      i2 <- dispositionsOutput(incomeIntervals(Between50And79PercentOfMSAMedian))
      i3 <- dispositionsOutput(incomeIntervals(Between80And99PercentOfMSAMedian))
      i4 <- dispositionsOutput(incomeIntervals(Between100And119PercentOfMSAMedian))
      i5 <- dispositionsOutput(incomeIntervals(GreaterThan120PercentOfMSAMedian))
      i6 <- dispositionsOutput(lars.filter(lar => lar.applicant.income == "NA"))

    } yield {
      val report = s"""
         |{
         |    "table": "${metaData.reportTable}",
         |    "type": "${metaData.reportType}",
         |    "description": "${metaData.description}",
         |    "year": "$year",
         |    "reportDate": "$reportDate",
         |    $msa
         |    "applicantCharacteristics": [
         |        {
         |            "characteristic": "race",
         |            "races": [
         |                {
         |                    "race": "American Indian/Alaska Native",
         |                    "denialReasons": $r1
         |                },
         |                {
         |                    "race": "Asian",
         |                    "denialReasons": $r2
         |                },
         |                {
         |                    "race": "Black or African American",
         |                    "denialReasons": $r3
         |                },
         |                {
         |                    "race": "Native Hawaiian or Other Pacific Islander",
         |                    "denialReasons": $r4
         |                },
         |                {
         |                    "race": "White",
         |                    "denialReasons": $r5
         |                },
         |                {
         |                    "race": "2 or more minority races",
         |                    "denialReasons": $r6
         |                },
         |                {
         |                    "race": "Joint (White/Minority Race)",
         |                    "denialReasons": $r7
         |                },
         |                {
         |                    "race": "Race Not Available",
         |                    "denialReasons": $r8
         |                }
         |            ]
         |        },
         |        {
         |            "characteristic": "ethnicity",
         |            "ethnicities": [
         |                {
         |                    "ethnicity": "Hispanic or Latino",
         |                    "denialReasons": $e1
         |                },
         |                {
         |                    "ethnicity": "Not Hispanic or Latino",
         |                    "denialReasons": $e2
         |                },
         |                {
         |                    "ethnicity": "Joint (Hispanic or Latino/Not Hispanic or Latino)",
         |                    "denialReasons": $e3
         |                },
         |                {
         |                    "ethnicity": "Ethnicity Not Available",
         |                    "denialReasons": $e4
         |                }
         |            ]
         |        },
         |        {
         |            "characteristic": "minorityStatus",
         |            "minorityStatuses": [
         |                {
         |                    "minorityStatus": "White Non-Hispanic",
         |                    "denialReasons": $m1
         |                },
         |                {
         |                    "minorityStatus": "Others, Including Hispanic",
         |                    "denialReasons": $m2
         |                }
         |            ]
         |        },
         |        {
         |            "characteristic": "gender",
         |            "genders": [
         |                {
         |                    "gender": "Male",
         |                    "denialReasons": $g1
         |                },
         |                {
         |                    "gender": "Female",
         |                    "denialReasons": $g2
         |                },
         |                {
         |                    "gender": "Joint (Male/Female)",
         |                    "denialReasons": $g3
         |                },
         |                {
         |                    "gender": "Gender Not Available",
         |                    "denialReasons": $g4
         |                }
         |            ]
         |        },
         |        {
         |            "characteristic": "income",
         |            "incomes": [
         |                {
         |                    "income": "Less than 50% of MSA/MD median",
         |                    "denialReasons": $i1
         |                },
         |                {
         |                    "income": "50-79% of MSA/MD median",
         |                    "denialReasons": $i2
         |                },
         |                {
         |                    "income": "80-99% of MSA/MD median",
         |                    "denialReasons": $i3
         |                },
         |                {
         |                    "income": "100-119% of MSA/MD median",
         |                    "denialReasons": $i4
         |                },
         |                {
         |                    "income": "120% or more of MSA/MD median",
         |                    "denialReasons": $i5
         |                },
         |                {
         |                    "income": "Income Not Available",
         |                    "denialReasons": $i6
         |                }
         |            ]
         |        }
         |    ]
         |
         |}
         |
       """.stripMargin

      val fipsString = if (metaData.reportType == Aggregate) fipsCode.toString else "nationwide"

      AggregateReportPayload(metaData.reportTable, fipsString, report)
    }
  }

  private def dispositionsOutput[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    calculatePercentageDispositions(larSource, dispositions, TotalDenied).map { list =>
      PercentageDisposition.collectionJson(list)
    }
  }

}
