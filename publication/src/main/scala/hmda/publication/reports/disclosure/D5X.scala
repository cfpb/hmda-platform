package hmda.publication.reports.disclosure

import hmda.publication.reports.util.ReportsMetaDataLookup
import hmda.publication.reports.util.ReportUtil._
import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.model.publication.reports.ApplicantIncomeEnum._
import hmda.model.publication.reports.EthnicityEnum.{ HispanicOrLatino, JointEthnicity, NotAvailable, NotHispanicOrLatino }
import hmda.model.publication.reports.MinorityStatusEnum.{ OtherIncludingHispanic, WhiteNonHispanic }
import hmda.model.publication.reports.RaceEnum._
import hmda.model.publication.reports.ValueDisposition
import hmda.publication.reports._
import hmda.publication.reports.util.DispositionType._
import hmda.publication.reports.util.EthnicityUtil.filterEthnicity
import hmda.publication.reports.util.MinorityStatusUtil.filterMinorityStatus
import hmda.publication.reports.util.RaceUtil.filterRace

import scala.concurrent.Future

object D51 extends D5X {
  val reportId = "D51"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.loanType == 2 || lar.loan.loanType == 3 || lar.loan.loanType == 4) &&
      (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 1)
  }
}

object D52 extends D5X {
  val reportId = "D52"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.loanType == 1) &&
      (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 1)
  }
}

object D53 extends D5X {
  val reportId = "D53"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 3)
  }
}

object D54 extends D5X {
  val reportId = "D54"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 2)
  }
}

object D56 extends D5X {
  val reportId = "D56"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.occupancy == 2 &&
      (loan.propertyType == 1 || loan.propertyType == 2) &&
      (loan.purpose == 1 || loan.purpose == 2 || loan.purpose == 3)
  }
}

object D57 extends D5X {
  val reportId = "D57"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.propertyType == 2 &&
      (loan.purpose == 1 || loan.purpose == 2 || loan.purpose == 3)
  }
}

trait D5X extends DisclosureReport {
  val reportId: String
  def filters(lar: LoanApplicationRegister): Boolean
  val dispositions = List(ApplicationReceived, LoansOriginated, ApprovedButNotAccepted,
    ApplicationsDenied, ApplicationsWithdrawn, ClosedForIncompleteness)

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

    val larsWithIncome = lars.filter(lar => lar.applicant.income != "NA")

    val msa = msaReport(fipsCode.toString).toJsonFormat

    val incomeIntervals = larsByIncomeInterval(
      lars.filter(_.applicant.income != "NA"),
      calculateMedianIncomeIntervals(fipsCode)
    )
    val yearF = calculateYear(larSource)
    val reportDate = formattedCurrentDate

    for {
      year <- yearF

      ri1 <- raceDispositions(incomeIntervals(LessThan50PercentOfMSAMedian))
      ei1 <- ethnicityDispositions(incomeIntervals(LessThan50PercentOfMSAMedian))
      mi1 <- minorityStatusDispositions(incomeIntervals(LessThan50PercentOfMSAMedian))

      ri2 <- raceDispositions(incomeIntervals(Between50And79PercentOfMSAMedian))
      ei2 <- ethnicityDispositions(incomeIntervals(Between50And79PercentOfMSAMedian))
      mi2 <- minorityStatusDispositions(incomeIntervals(Between50And79PercentOfMSAMedian))

      ri3 <- raceDispositions(incomeIntervals(Between80And99PercentOfMSAMedian))
      ei3 <- ethnicityDispositions(incomeIntervals(Between80And99PercentOfMSAMedian))
      mi3 <- minorityStatusDispositions(incomeIntervals(Between80And99PercentOfMSAMedian))

      ri4 <- raceDispositions(incomeIntervals(Between100And119PercentOfMSAMedian))
      ei4 <- ethnicityDispositions(incomeIntervals(Between100And119PercentOfMSAMedian))
      mi4 <- minorityStatusDispositions(incomeIntervals(Between100And119PercentOfMSAMedian))

      ri5 <- raceDispositions(incomeIntervals(GreaterThan120PercentOfMSAMedian))
      ei5 <- ethnicityDispositions(incomeIntervals(GreaterThan120PercentOfMSAMedian))
      mi5 <- minorityStatusDispositions(incomeIntervals(GreaterThan120PercentOfMSAMedian))

      total <- dispositionsOutput(larsWithIncome)
    } yield {

      val report =
        s"""
           |{
           |    "respondentId": "${institution.respondentId}",
           |    "institutionName": "${institution.respondent.name}",
           |    "table": "${metaData.reportTable}",
           |    "type": "Disclosure",
           |    "description": "${metaData.description}",
           |    "year": "$year",
           |    "reportDate": "$reportDate",
           |    "msa": $msa,
           |    "applicantIncomes": [
           |        {
           |            "applicantIncome": "Less than 50% of MSA/MD median",
           |            "borrowerCharacteristics": [
           |                {
           |                    "characteristic": "Race",
           |                    "races": $ri1
           |                },
           |                {
           |                    "characteristic": "Ethnicity",
           |                    "ethnicities": $ei1
           |                },
           |                {
           |                    "characteristic": "Minority Status",
           |                    "minorityStatus": $mi1
           |                }
           |            ]
           |        },
           |        {
           |            "applicantIncome": "50-79% of MSA/MD median",
           |            "borrowerCharacteristics": [
           |                {
           |                    "characteristic": "Race",
           |                    "races": $ri2
           |                },
           |                {
           |                    "characteristic": "Ethnicity",
           |                    "ethnicities": $ei2
           |                },
           |                {
           |                    "characteristic": "Minority Status",
           |                    "minorityStatus": $mi2
           |                }
           |            ]
           |        },
           |        {
           |            "applicantIncome": "80-99% of MSA/MD median",
           |            "borrowerCharacteristics": [
           |                {
           |                    "characteristic": "Race",
           |                    "races": $ri3
           |                },
           |                {
           |                    "characteristic": "Ethnicity",
           |                    "ethnicities": $ei3
           |                },
           |                {
           |                    "characteristic": "Minority Status",
           |                    "minorityStatus": $mi3
           |                }
           |            ]
           |        },
           |        {
           |            "applicantIncome": "100-119% of MSA/MD median",
           |            "borrowerCharacteristics": [
           |                {
           |                    "characteristic": "Race",
           |                    "races": $ri4
           |                },
           |                {
           |                    "characteristic": "Ethnicity",
           |                    "ethnicities": $ei4
           |                },
           |                {
           |                    "characteristic": "Minority Status",
           |                    "minorityStatus": $mi4
           |                }
           |            ]
           |        },
           |        {
           |            "applicantIncome": "120% or more of MSA/MD median",
           |            "borrowerCharacteristics": [
           |                {
           |                    "characteristic": "Race",
           |                    "races": $ri5
           |                },
           |                {
           |                    "characteristic": "Ethnicity",
           |                    "ethnicities": $ei5
           |                },
           |                {
           |                    "characteristic": "Minority Status",
           |                    "minorityStatus": $mi5
           |                }
           |            ]
           |        }
           |    ],
           |    "total": $total
           |}
         """.stripMargin

      DisclosureReportPayload(metaData.reportTable, fipsCode.toString, report)
    }

  }

  private def raceDispositions[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    val races = List(AmericanIndianOrAlaskaNative, Asian, BlackOrAfricanAmerican,
      HawaiianOrPacific, White, TwoOrMoreMinority, JointRace, NotProvided)

    val raceOutputs: Future[List[String]] = Future.sequence(
      races.map { race =>
        dispositionsOutput(filterRace(larSource, race)).map { disp =>
          s"""
             |{
             |    "race": "${race.description}",
             |    "dispositions": $disp
             |}
          """.stripMargin
        }
      }
    )

    raceOutputs.map { list => list.mkString("[", ",", "]") }
  }
  private def ethnicityDispositions[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    val ethnicities = List(HispanicOrLatino, NotHispanicOrLatino, JointEthnicity, NotAvailable)

    val ethnicityOutputs: Future[List[String]] = Future.sequence(
      ethnicities.map { ethnicity =>
        dispositionsOutput(filterEthnicity(larSource, ethnicity)).map { disp =>
          s"""
             |{
             |    "ethnicity": "${ethnicity.description}",
             |    "dispositions": $disp
             |}
          """.stripMargin
        }
      }
    )

    ethnicityOutputs.map { list => list.mkString("[", ",", "]") }
  }
  private def minorityStatusDispositions[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    val minorityStatuses = List(WhiteNonHispanic, OtherIncludingHispanic)

    val minorityStatusOutputs: Future[List[String]] = Future.sequence(
      minorityStatuses.map { minorityStatus =>
        dispositionsOutput(filterMinorityStatus(larSource, minorityStatus)).map { disp =>
          s"""
             |{
             |    "minorityStatus": "${minorityStatus.description}",
             |    "dispositions": $disp
             |}
          """.stripMargin
        }
      }
    )

    minorityStatusOutputs.map { list => list.mkString("[", ",", "]") }
  }

  private def dispositionsOutput[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    val calculatedDispositions: Future[List[ValueDisposition]] = Future.sequence(
      dispositions.map(_.calculateValueDisposition(larSource))
    )

    calculatedDispositions.map(list => list.map(_.toJsonFormat).mkString("[", ",", "]"))
  }
}
