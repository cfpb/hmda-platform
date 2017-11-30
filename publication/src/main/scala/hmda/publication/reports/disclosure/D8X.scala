package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.ValueDisposition
import hmda.model.publication.reports.EthnicityEnum._
import hmda.model.publication.reports.GenderEnum._
import hmda.model.publication.reports.ApplicantIncomeEnum._
import hmda.model.publication.reports.MinorityStatusEnum._
import hmda.model.publication.reports.RaceEnum._
import hmda.publication.reports._
import hmda.publication.reports.util.DispositionType._
import hmda.publication.reports.util.EthnicityUtil.filterEthnicity
import hmda.publication.reports.util.GenderUtil.filterGender
import hmda.publication.reports.util.MinorityStatusUtil.filterMinorityStatus
import hmda.publication.reports.util.RaceUtil.filterRace
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.ReportsMetaDataLookup

import scala.concurrent.Future
import spray.json._

trait D8X {
  val reportId: String
  def filters(lar: LoanApplicationRegister): Boolean
}

object D81 extends D8X {
  val reportId = "D81"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.loanType == 2 || lar.loan.loanType == 3 || lar.loan.loanType == 4) &&
      (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 1)
  }
}

object D82 extends D8X {
  val reportId = "D82"
  def filters(lar: LoanApplicationRegister): Boolean = {
    lar.loan.loanType == 1 &&
      (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 1)
  }
}

object D83 extends D8X {
  val reportId = "D83"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 3)
  }
}

object D84 extends D8X {
  val reportId = "D84"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 2)
  }
}

object D85 extends D8X {
  val reportId = "D85"
  def filters(lar: LoanApplicationRegister): Boolean = lar.loan.propertyType == 3
}

object D86 extends D8X {
  val reportId = "D86"
  def filters(lar: LoanApplicationRegister): Boolean = {
    lar.loan.occupancy == 2 &&
      (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 1 || lar.loan.purpose == 2 || lar.loan.purpose == 3)
  }
}

object D87 extends D8X {
  val reportId = "D87"
  def filters(lar: LoanApplicationRegister): Boolean = {
    lar.loan.propertyType == 2 &&
      (lar.loan.purpose == 1 || lar.loan.purpose == 2 || lar.loan.purpose == 3)
  }
}

object D8X {
  val dispositions = List(DebtToIncomeRatio, EmploymentHistory, CreditHistory,
    Collateral, InsufficientCash, UnverifiableInformation, CreditAppIncomplete,
    MortgageInsuranceDenied, OtherDenialReason, TotalDenied)

  def generate[ec: EC, mat: MAT, as: AS](
    report: D8X,
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int,
    respondentId: String,
    institutionNameF: Future[String]
  ): Future[JsValue] = {

    val metaData = ReportsMetaDataLookup.values(report.reportId)

    val lars = larSource
      .filter(lar => lar.respondentId == respondentId)
      .filter(lar => lar.geography.msa != "NA")
      .filter(lar => lar.geography.msa.toInt == fipsCode)
      .filter(report.filters)

    val incomeIntervals = larsByIncomeInterval(
      lars.filter(lar => lar.applicant.income != "NA"),
      calculateMedianIncomeIntervals(fipsCode)
    )
    val msa = msaReport(fipsCode.toString).toJsonFormat
    val reportDate = formattedCurrentDate
    val yearF = calculateYear(lars)

    for {
      institutionName <- institutionNameF
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
      s"""
         |{
         |    "respondentId": "$respondentId",
         |    "institutionName": "$institutionName",
         |    "table": "${metaData.reportTable}",
         |    "type": "Disclosure",
         |    "description": "${metaData.description}",
         |    "year": "$year",
         |    "reportDate": "$reportDate",
         |    "msa": $msa,
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
       """.stripMargin.parseJson
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
