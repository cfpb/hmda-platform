package hmda.publication.reports.disclosure

import hmda.model.publication.reports.{ ApplicantIncome, MSAReport, ValueDisposition }
import hmda.publication.reports.util.ReportsMetaDataLookup
import hmda.publication.reports.util.ReportUtil._
import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.publication.reports._
import hmda.publication.reports.protocol.disclosure.D5XProtocol._

import scala.concurrent.Future
import spray.json._

case class D5X(
  respondentId: String,
  institutionName: String,
  year: Int,
  msa: MSAReport,
  applicantIncomes: List[ApplicantIncome],
  total: List[ValueDisposition],
  table: String,
  description: String,
  reportDate: String = formattedCurrentDate
)

object D5X {
  def generateD5X[ec: EC, mat: MAT, as: AS](
    reportId: String,
    filters: LoanApplicationRegister => Boolean,
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int,
    institution: Institution
  ): Future[DisclosureReportPayload] = {

    val metaData = ReportsMetaDataLookup.values(reportId)
    val dispositions = metaData.dispositions

    val lars = larSource
      .filter(lar => lar.geography.msa != "NA")
      .filter(lar => lar.geography.msa.toInt == fipsCode)
      .filter(filters)

    val larsWithIncome = lars.filter(lar => lar.applicant.income != "NA")

    val msa = msaReport(fipsCode.toString)

    val incomeIntervals = calculateMedianIncomeIntervals(fipsCode)
    val applicantIncomesF = applicantIncomesWithBorrowerCharacteristics(larsWithIncome, incomeIntervals, dispositions)

    val yearF = calculateYear(larSource)
    val totalF = calculateDispositions(lars, dispositions)

    for {
      year <- yearF
      applicantIncomes <- applicantIncomesF
      total <- totalF
    } yield {

      val report = D5X(
        institution.respondentId,
        institution.respondent.name,
        year,
        msa,
        applicantIncomes,
        total,
        metaData.reportTable,
        metaData.description
      ).toJson.toString
      val reportContent =
        s"""
           |{
           |    "respondentId": "0000451965",
           |    "institutionName": "WELLS FARGO BANK, NA",
           |    "table": "5-1",
           |    "type": "Disclosure",
           |    "description": "Disposition of applications for FHA, FSA/RHS, and VA home-purchase loans, 1- to 4-family and manufactured home dwellings, by income, race and ethnicity of applicant",
           |    "year": "2013",
           |    "reportDate": "05/27/2015",
           |    "msa": {
           |        "id": "31084",
           |        "name": "Los_Angeles-Long_Beach-Santa_Ana",
           |        "state": "CA",
           |        "stateName": "California"
           |    },
           |    "applicantIncomes": [
           |        {
           |            "applicantIncome": "Less than 50% of MSA/MD median",
           |            "borrowerCharacteristics": [
           |                {
           |                    "characteristic": "Race",
           |                    "races": [
           |                        {
           |                            "race": "American Indian/Alaska Native",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Asian",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Black or African American",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Native Hawaiian or Other Pacific Islander",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "White",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "2 or more minority races",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Joint (White/Minority Race)",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Race Not Available",
           |                            "dispositions": $r1
           |                        }
           |                    ]
           |                },
           |                {
           |                    "characteristic": "Ethnicity",
           |                    "ethnicities": [
           |                        {
           |                            "ethnicity": "Hispanic or Latino",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "ethnicity": "Not Hispanic or Latino",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "ethnicity": "Joint (Hispanic or Latino/Not Hispanic or Latino)",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "ethnicity": "Ethnicity Not Available",
           |                            "dispositions": $r1
           |                        }
           |                    ]
           |                },
           |                {
           |                    "characteristic": "Minority Status",
           |                    "minorityStatus": [
           |                        {
           |                            "minorityStatus": "White Non-Hispanic",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "minorityStatus": "Others, Including Hispanic",
           |                            "dispositions": $r1
           |                        }
           |                    ]
           |                }
           |            ]
           |        },
           |        {
           |            "applicantIncome": "50-79% of MSA/MD median",
           |            "borrowerCharacteristics": [
           |                {
           |                    "characteristic": "Race",
           |                    "races": [
           |                        {
           |                            "race": "American Indian/Alaska Native",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Asian",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Black or African American",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Native Hawaiian or Other Pacific Islander",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "White",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "2 or more minority races",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Joint (White/Minority Race)",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Race Not Available",
           |                            "dispositions": $r1
           |                        }
           |                    ]
           |                },
           |                {
           |                    "characteristic": "Ethnicity",
           |                    "ethnicities": [
           |                        {
           |                            "ethnicity": "Hispanic or Latino",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "ethnicity": "Not Hispanic or Latino",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "ethnicity": "Joint (Hispanic or Latino/Not Hispanic or Latino)",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "ethnicity": "Ethnicity Not Available",
           |                            "dispositions": $r1
           |                        }
           |                    ]
           |                },
           |                {
           |                    "characteristic": "Minority Status",
           |                    "minorityStatus": [
           |                        {
           |                            "minorityStatus": "White Non-Hispanic",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "minorityStatus": "Others, Including Hispanic",
           |                            "dispositions": $r1
           |                        }
           |                    ]
           |                }
           |            ]
           |        },
           |        {
           |            "applicantIncome": "80-99% of MSA/MD median",
           |            "borrowerCharacteristics": [
           |                {
           |                    "characteristic": "Race",
           |                    "races": [
           |                        {
           |                            "race": "American Indian/Alaska Native",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Asian",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Black or African American",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Native Hawaiian or Other Pacific Islander",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "White",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "2 or more minority races",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Joint (White/Minority Race)",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Race Not Available",
           |                            "dispositions": $r1
           |                        }
           |                    ]
           |                },
           |                {
           |                    "characteristic": "Ethnicity",
           |                    "ethnicities": [
           |                        {
           |                            "ethnicity": "Hispanic or Latino",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "ethnicity": "Not Hispanic or Latino",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "ethnicity": "Joint (Hispanic or Latino/Not Hispanic or Latino)",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "ethnicity": "Ethnicity Not Available",
           |                            "dispositions": $r1
           |                        }
           |                    ]
           |                },
           |                {
           |                    "characteristic": "Minority Status",
           |                    "minorityStatus": [
           |                        {
           |                            "minorityStatus": "White Non-Hispanic",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "minorityStatus": "Others, Including Hispanic",
           |                            "dispositions": $r1
           |                        }
           |                    ]
           |                }
           |            ]
           |        },
           |        {
           |            "applicantIncome": "100-119% of MSA/MD median",
           |            "borrowerCharacteristics": [
           |                {
           |                    "characteristic": "Race",
           |                    "races": [
           |                        {
           |                            "race": "American Indian/Alaska Native",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Asian",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Black or African American",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Native Hawaiian or Other Pacific Islander",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "White",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "2 or more minority races",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Joint (White/Minority Race)",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Race Not Available",
           |                            "dispositions": $r1
           |                        }
           |                    ]
           |                },
           |                {
           |                    "characteristic": "Ethnicity",
           |                    "ethnicities": [
           |                        {
           |                            "ethnicity": "Hispanic or Latino",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "ethnicity": "Not Hispanic or Latino",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "ethnicity": "Joint (Hispanic or Latino/Not Hispanic or Latino)",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "ethnicity": "Ethnicity Not Available",
           |                            "dispositions": $r1
           |                        }
           |                    ]
           |                },
           |                {
           |                    "characteristic": "Minority Status",
           |                    "minorityStatus": [
           |                        {
           |                            "minorityStatus": "White Non-Hispanic",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "minorityStatus": "Others, Including Hispanic",
           |                            "dispositions": $r1
           |                        }
           |                    ]
           |                }
           |            ]
           |        },
           |        {
           |            "applicantIncome": "120% or more of MSA/MD median",
           |            "borrowerCharacteristics": [
           |                {
           |                    "characteristic": "Race",
           |                    "races": [
           |                        {
           |                            "race": "American Indian/Alaska Native",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Asian",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Black or African American",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Native Hawaiian or Other Pacific Islander",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "White",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "2 or more minority races",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Joint (White/Minority Race)",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "race": "Race Not Available",
           |                            "dispositions": $r1
           |                        }
           |                    ]
           |                },
           |                {
           |                    "characteristic": "Ethnicity",
           |                    "ethnicities": [
           |                        {
           |                            "ethnicity": "Hispanic or Latino",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "ethnicity": "Not Hispanic or Latino",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "ethnicity": "Joint (Hispanic or Latino/Not Hispanic or Latino)",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "ethnicity": "Ethnicity Not Available",
           |                            "dispositions": $r1
           |                        }
           |                    ]
           |                },
           |                {
           |                    "characteristic": "Minority Status",
           |                    "minorityStatus": [
           |                        {
           |                            "minorityStatus": "White Non-Hispanic",
           |                            "dispositions": $r1
           |                        },
           |                        {
           |                            "minorityStatus": "Others, Including Hispanic",
           |                            "dispositions": $r1
           |                        }
           |                    ]
           |                }
           |            ]
           |        }
           |    ],
           |    "total": $r1
           |}
         """.stripMargin

    }

  }
}
