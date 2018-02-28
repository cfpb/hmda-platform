package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.census.model.{ Tract, TractLookup }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.model.publication.reports.ApplicantIncomeEnum._
import hmda.model.publication.reports.EthnicityEnum._
import hmda.model.publication.reports.GenderEnum._
import hmda.model.publication.reports.MinorityStatusEnum._
import hmda.model.publication.reports.RaceEnum._
import hmda.model.publication.reports.ValueDisposition
import hmda.publication.reports._
import hmda.publication.reports.util.CensusTractUtil._
import hmda.publication.reports.util.DispositionType.{ PreApprovalsDenied, PreapprovalsNotAccepted, PreapprovalsToOriginations }
import hmda.publication.reports.util.EthnicityUtil.filterEthnicity
import hmda.publication.reports.util.GenderUtil.filterGender
import hmda.publication.reports.util.MinorityStatusUtil.filterMinorityStatus
import hmda.publication.reports.util.RaceUtil.filterRace
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.ReportsMetaDataLookup

import scala.concurrent.Future

object A4W extends DisclosureReport {
  val reportId: String = "DA4W"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.loanType == 1 && loan.purpose == 1 && lar.lienStatus == 1 &&
      loan.propertyType == 1
  }

  val dispositions = List(PreapprovalsToOriginations, PreapprovalsNotAccepted, PreApprovalsDenied)

  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int,
    institution: Institution
  ): Future[DisclosureReportPayload] = {

    val metaData = ReportsMetaDataLookup.values(reportId)

    val lars = larSource
      .filter(filters)

    val incomeIntervals = larsByIncomeInterval(
      lars.filter(lar => lar.applicant.income != "NA"),
      calculateMedianIncomeIntervals(fipsCode)
    )
    val msa = msaReport(fipsCode.toString).toJsonFormat
    val reportDate = formattedCurrentDate
    val yearF = calculateYear(lars)

    val msaTracts: Set[Tract] = TractLookup.values.filter(_.msa == fipsCode.toString)

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

      tractMinorityComposition1 <- dispositionsOutput(filterMinorityPopulation(lars, 0, 10, msaTracts))
      tractMinorityComposition2 <- dispositionsOutput(filterMinorityPopulation(lars, 10, 20, msaTracts))
      tractMinorityComposition3 <- dispositionsOutput(filterMinorityPopulation(lars, 20, 50, msaTracts))
      tractMinorityComposition4 <- dispositionsOutput(filterMinorityPopulation(lars, 50, 80, msaTracts))
      tractMinorityComposition5 <- dispositionsOutput(filterMinorityPopulation(lars, 80, 101, msaTracts))

      tractIncome1 <- dispositionsOutput(filterIncomeCharacteristics(lars, 0, 50, msaTracts))
      tractIncome2 <- dispositionsOutput(filterIncomeCharacteristics(lars, 50, 80, msaTracts))
      tractIncome3 <- dispositionsOutput(filterIncomeCharacteristics(lars, 80, 120, msaTracts))
      tractIncome4 <- dispositionsOutput(filterIncomeCharacteristics(lars, 120, 1000, msaTracts))

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
       |    "borrowerCharacteristics": [
       |        {
       |            "characteristic": "Race",
       |            "races": [
       |                {
       |                    "race": "American Indian/Alaska Native",
       |                    "preapprovalStatuses": $r1
       |                },
       |                {
       |                    "race": "Asian",
       |                    "preapprovalStatuses": $r2
       |                },
       |                {
       |                    "race": "Black or African American",
       |                    "preapprovalStatuses": $r3
       |                },
       |                {
       |                    "race": "Native Hawaiian or Other Pacific Islander",
       |                    "preapprovalStatuses": $r4
       |                },
       |                {
       |                    "race": "White",
       |                    "preapprovalStatuses": $r5
       |                },
       |                {
       |                    "race": "2 or more minority races",
       |                    "preapprovalStatuses": $r6
       |                },
       |                {
       |                    "race": "Joint (White/Minority Race)",
       |                    "preapprovalStatuses": $r7
       |                },
       |                {
       |                    "race": "Race Not Available",
       |                    "preapprovalStatuses": $r8
       |                }
       |            ]
       |        },
       |        {
       |            "characteristic": "Ethnicity",
       |            "ethnicities": [
       |                {
       |                    "ethnicity": "Hispanic or Latino",
       |                    "preapprovalStatuses": $e1
       |                },
       |                {
       |                    "ethnicity": "Not Hispanic or Latino",
       |                    "preapprovalStatuses": $e2
       |                },
       |                {
       |                    "ethnicity": "Joint (Hispanic or Latino/Not Hispanic or Latino)",
       |                    "preapprovalStatuses": $e3
       |                },
       |                {
       |                    "ethnicity": "Ethnicity Not Available",
       |                    "preapprovalStatuses": $e4
       |                }
       |            ]
       |        },
       |        {
       |            "characteristic": "Minority Status",
       |            "minorityStatuses": [
       |                {
       |                    "minorityStatus": "White Non-Hispanic",
       |                    "preapprovalStatuses": $m1
       |                },
       |                {
       |                    "minorityStatus": "Others, Including Hispanic",
       |                    "preapprovalStatuses": $m2
       |                }
       |            ]
       |        },
       |        {
       |            "characteristic": "Income",
       |            "incomes": [
       |                {
       |                    "income": "Less than 50% of MSA/MD median",
       |                    "preapprovalStatuses": $i1
       |                },
       |                {
       |                    "income": "50-79% of MSA/MD median",
       |                    "preapprovalStatuses": $i2
       |                },
       |                {
       |                    "income": "80-99% of MSA/MD median",
       |                    "preapprovalStatuses": $i3
       |                },
       |                {
       |                    "income": "100-119% of MSA/MD median",
       |                    "preapprovalStatuses": $i4
       |                },
       |                {
       |                    "income": "120% or more of MSA/MD median",
       |                    "preapprovalStatuses": $i5
       |                },
       |                {
       |                    "income": "Income Not Available",
       |                    "preapprovalStatuses": $i6
       |                }
       |            ]
       |        },
       |        {
       |            "characteristic": "Gender",
       |            "genders": [
       |                {
       |                    "gender": "Male",
       |                    "preapprovalStatuses": $g1
       |                },
       |                {
       |                    "gender": "Female",
       |                    "preapprovalStatuses": $g2
       |                },
       |                {
       |                    "gender": "Joint (Male/Female)",
       |                    "preapprovalStatuses": $g3
       |                },
       |                {
       |                    "gender": "Gender Not Available",
       |                    "preapprovalStatuses": $g4
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
       |                    "preapprovalStatuses": $tractMinorityComposition1
       |                },
       |                {
       |                    "composition": "10-19% minority",
       |                    "preapprovalStatuses": $tractMinorityComposition2
       |                },
       |                {
       |                    "composition": "20-49% minority",
       |                    "preapprovalStatuses": $tractMinorityComposition3
       |                },
       |                {
       |                    "composition": "50-79% minority",
       |                    "preapprovalStatuses": $tractMinorityComposition4
       |                },
       |                {
       |                    "composition": "80-100% minority",
       |                    "preapprovalStatuses": $tractMinorityComposition5
       |                }
       |            ]
       |        },
       |        {
       |            "characteristic": "Income Characteristics",
       |            "incomes": [
       |                {
       |                    "income": "Low income",
       |                    "preapprovalStatuses": $tractIncome1
       |                },
       |                {
       |                    "income": "Moderate income",
       |                    "preapprovalStatuses": $tractIncome2
       |                },
       |                {
       |                    "income": "Middle income",
       |                    "preapprovalStatuses": $tractIncome3
       |                },
       |                {
       |                    "income": "Upper income",
       |                    "preapprovalStatuses": $tractIncome4
       |                }
       |            ]
       |        }
       |    ]
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
