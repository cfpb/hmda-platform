package hmda.publication.reports.disclosure
import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.census.model.{ Tract, TractLookup }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.model.publication.reports.ApplicantIncomeEnum._
import hmda.model.publication.reports.EthnicityEnum.{ HispanicOrLatino, JointEthnicity, NotAvailable, NotHispanicOrLatino }
import hmda.model.publication.reports.MinorityStatusEnum.{ OtherIncludingHispanic, WhiteNonHispanic }
import hmda.model.publication.reports.RaceEnum._
import hmda.model.publication.reports.ValueDisposition
import hmda.publication.reports.util.CensusTractUtil._
import hmda.publication.reports.util.EthnicityUtil._
import hmda.publication.reports.util.MinorityStatusUtil._
import hmda.publication.reports.util.RaceUtil._
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.DispositionType._
import hmda.publication.reports.util.ReportsMetaDataLookup
import hmda.publication.reports.{ AS, EC, MAT }

import scala.concurrent.Future

object D31 extends DisclosureReport {
  val reportId = "D31"
  def filters(lar: LoanApplicationRegister): Boolean = (1 to 9).contains(lar.purchaserType)

  val dispositions = List(FannieMae, GinnieMae, FreddieMac,
    FarmerMac, PrivateSecuritization, CommercialBank,
    FinanceCompany, Affiliate, OtherPurchaser)

  override def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int,
    institution: Institution,
    msaList: List[Int]
  ): Future[DisclosureReportPayload] = {

    val metaData = ReportsMetaDataLookup.values(reportId)

    val lars = larSource
      .filter(lar => lar.geography.msa != "NA")
      .filter(lar => lar.geography.msa.toInt == fipsCode)
      .filter(filters)

    val incomeIntervals = larsByIncomeInterval(
      lars.filter(lar => lar.applicant.income != "NA"),
      calculateMedianIncomeIntervals(fipsCode)
    )

    val msaTracts: Set[Tract] = TractLookup.values.filter(_.msa == fipsCode.toString)

    val lowIncomeLars = filterIncomeCharacteristics(lars, 0, 50, msaTracts)
    val moderateIncomeLars = filterIncomeCharacteristics(lars, 50, 80, msaTracts)
    val middleIncomeLars = filterIncomeCharacteristics(lars, 80, 120, msaTracts)
    val upperIncomeLars = filterIncomeCharacteristics(lars, 120, 1000, msaTracts)

    val msa = msaReport(fipsCode.toString).toJsonFormat
    val reportDate = formattedCurrentDate
    val yearF = calculateYear(lars)

    for {
      year <- yearF

      r1 <- dispositionsOutput(filterRace(lars, AmericanIndianOrAlaskaNative))
      r2 <- dispositionsOutput(filterRace(lars, Asian))
      r3 <- dispositionsOutput(filterRace(lars, BlackOrAfricanAmerican))
      r4 <- dispositionsOutput(filterRace(lars, HawaiianOrPacific))
      r5 <- dispositionsOutput(filterRace(lars, White))
      r6 <- dispositionsOutput(filterRace(lars, TwoOrMoreMinority))
      r7 <- dispositionsOutput(filterRace(lars, JointRace))
      r8 <- dispositionsOutput(filterRace(lars, NotProvided))

      e1 <- dispositionsOutput(filterEthnicity(lars, HispanicOrLatino))
      e2 <- dispositionsOutput(filterEthnicity(lars, NotHispanicOrLatino))
      e3 <- dispositionsOutput(filterEthnicity(lars, JointEthnicity))
      e4 <- dispositionsOutput(filterEthnicity(lars, NotAvailable))

      m1 <- dispositionsOutput(filterMinorityStatus(lars, WhiteNonHispanic))
      m2 <- dispositionsOutput(filterMinorityStatus(lars, OtherIncludingHispanic))

      i1 <- dispositionsOutput(incomeIntervals(LessThan50PercentOfMSAMedian))
      i2 <- dispositionsOutput(incomeIntervals(Between50And79PercentOfMSAMedian))
      i3 <- dispositionsOutput(incomeIntervals(Between80And99PercentOfMSAMedian))
      i4 <- dispositionsOutput(incomeIntervals(Between100And119PercentOfMSAMedian))
      i5 <- dispositionsOutput(incomeIntervals(GreaterThan120PercentOfMSAMedian))
      i6 <- dispositionsOutput(lars.filter(_.applicant.income == "NA"))

      cm1 <- dispositionsOutput(filterMinorityPopulation(lars, 0, 10, msaTracts))
      cm2 <- dispositionsOutput(filterMinorityPopulation(lars, 10, 20, msaTracts))
      cm3 <- dispositionsOutput(filterMinorityPopulation(lars, 20, 50, msaTracts))
      cm4 <- dispositionsOutput(filterMinorityPopulation(lars, 50, 80, msaTracts))
      cm5 <- dispositionsOutput(filterMinorityPopulation(lars, 80, 100, msaTracts))

      ci1 <- dispositionsOutput(lowIncomeLars)
      ci2 <- dispositionsOutput(moderateIncomeLars)
      ci3 <- dispositionsOutput(middleIncomeLars)
      ci4 <- dispositionsOutput(upperIncomeLars)

      total <- dispositionsOutput(lars)
    } yield {
      val report = s"""
         |{
         |    "respondentId": "${institution.respondentId}",
         |    "institutionName": "${institution.respondent.name}",
         |    "table": "${metaData.reportTable}",
         |    "type": "Disclosure",
         |    "description": "${metaData.description}",
         |    "year": "$year ",
         |    "reportDate": "$reportDate",
         |    "msa": $msa,
         |    "borrowerCharacteristics": [
         |        {
         |            "characteristic": "Race",
         |            "races": [
         |                {
         |                    "race": "American Indian/Alaska Native",
         |                    "purchasers": $r1
         |                },
         |                {
         |                    "race": "Asian",
         |                    "purchasers": $r2
         |                },
         |                {
         |                    "race": "Black or African American",
         |                    "purchasers": $r3
         |                },
         |                {
         |                    "race": "Native Hawaiian or Other Pacific Islander",
         |                    "purchasers": $r4
         |                },
         |                {
         |                    "race": "White",
         |                    "purchasers": $r5
         |                },
         |                {
         |                    "race": "2 or more minority races",
         |                    "purchasers": $r6
         |                },
         |                {
         |                    "race": "Joint (White/Minority Race)",
         |                    "purchasers": $r7
         |                },
         |                {
         |                    "race": "Race Not Available",
         |                    "purchasers": $r8
         |                }
         |            ]
         |        },
         |        {
         |            "characteristic": "Ethnicity",
         |            "ethnicities": [
         |                {
         |                    "ethnicity": "Hispanic or Latino",
         |                    "purchasers": $e1
         |                },
         |                {
         |                    "ethnicity": "Not Hispanic or Latino",
         |                    "purchasers": $e2
         |                },
         |                {
         |                    "ethnicity": "Joint (Hispanic or Latino/Not Hispanic or Latino)",
         |                    "purchasers": $e3
         |                },
         |                {
         |                    "ethnicity": "Ethnicity Not Available",
         |                    "purchasers": $e4
         |                }
         |            ]
         |        },
         |        {
         |            "characteristic": "Minority Status",
         |            "minorityStatuses": [
         |                {
         |                    "minorityStatus": "White Non-Hispanic",
         |                    "purchasers": $m1
         |                },
         |                {
         |                    "minorityStatus": "Others, Including Hispanic",
         |                    "purchasers": $m2
         |                }
         |            ]
         |        },
         |        {
         |            "characteristic": "Applicant Income",
         |            "applicantIncomes": [
         |                {
         |                    "applicantIncome": "Less than 50% of MSA/MD median",
         |                    "purchasers": $i1
         |                },
         |                {
         |                    "applicantIncome": "50-79% of MSA/MD median",
         |                    "purchasers": $i2
         |                },
         |                {
         |                    "applicantIncome": "80-99% of MSA/MD median",
         |                    "purchasers": $i3
         |                },
         |                {
         |                    "applicantIncome": "100-119% of MSA/MD median",
         |                    "purchasers": $i4
         |                },
         |                {
         |                    "applicantIncome": "120% or more of MSA/MD median",
         |                    "purchasers": $i5
         |                },
         |                {
         |                    "applicantIncome": "Income Not Available",
         |                    "purchasers": $i6
         |                }
         |            ]
         |        }
         |    ],
         |    "censusCharacteristics": [
         |        {
         |            "characteristic": "Racial/Ethnic Composition",
         |            "tractPctMinorities": [
         |                {
         |                    "tractPctMinority": "Less than 10% minority",
         |                    "purchasers": $cm1
         |                },
         |                {
         |                    "tractPctMinority": "10-19% minority",
         |                    "purchasers": $cm2
         |                },
         |                {
         |                    "tractPctMinority": "20-49% minority",
         |                    "purchasers": $cm3
         |                },
         |                {
         |                    "tractPctMinority": "50-79% minority",
         |                    "purchasers": $cm4
         |                },
         |                {
         |                    "tractPctMinority": "80-100% minority",
         |                    "purchasers": $cm5
         |                }
         |            ]
         |        },
         |        {
         |            "characteristic": "Income",
         |            "incomeLevels": [
         |                {
         |                    "incomeLevel": "Low income",
         |                    "purchasers": $ci1
         |                },
         |                {
         |                    "incomeLevel": "Moderate income",
         |                    "purchasers": $ci2
         |                },
         |                {
         |                    "incomeLevel": "Middle income",
         |                    "purchasers": $ci3
         |                },
         |                {
         |                    "incomeLevel": "Upper income",
         |                    "purchasers": $ci4
         |                }
         |            ]
         |        }
         |    ],
         |    "total": {
         |        "purchasers": $total
         |    }
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
