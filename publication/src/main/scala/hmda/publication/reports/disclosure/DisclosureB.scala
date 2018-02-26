package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.stream.scaladsl.{ Sink, Source }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.publication.reports._
import hmda.publication.reports.util.PricingDataUtil.{ calculateMedian, pricingDataReported, rateSpread }
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.ReportsMetaDataLookup
import spray.json._

import scala.concurrent.Future

object DiscB extends DisclosureB {
  override val reportId: String = "DB"
  override def filters(lar: LoanApplicationRegister): Boolean = {
    lar.loan.loanType == 1 && lar.loan.occupancy == 1
  }
}

object DiscBW extends DisclosureB {
  override val reportId: String = "DBW"
  override def filters(lar: LoanApplicationRegister): Boolean = {
    lar.loan.loanType == 1 && lar.loan.occupancy == 1
  }
}

trait DisclosureB extends DisclosureReport {
  val reportId: String
  def filters(lar: LoanApplicationRegister): Boolean

  val dispositions = List()

  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int,
    institution: Institution
  ): Future[DisclosureReportPayload] = {

    val metaData = ReportsMetaDataLookup.values(reportId)

    val lars = larSource.filter { lar =>
      if (reportId == "DB") {
        lar.geography.msa.toInt == fipsCode &&
          lar.geography.msa != "NA"
      } else true
    }.filter(filters)

    val singleFamily = lars.filter(lar => lar.loan.propertyType == 1)
    val manufactured = lars.filter(lar => lar.loan.propertyType == 2)

    val msa = msaReport(fipsCode.toString).toJsonFormat
    val msaLine: String = if (reportId == "DB") s""""msa": $msa,""" else ""

    val reportDate = formattedCurrentDate
    val yearF = calculateYear(lars)

    for {
      singleFamilyP1 <- purposes(singleFamily.filter(_.rateSpread == "NA"), counts)
      singleFamilyP2 <- purposes(singleFamily.filter(pricingDataReported), counts)
      singleFamilyH1 <- purposes(singleFamily.filter(_.hoepaStatus == 1), counts)
      singleFamilyH2 <- purposes(singleFamily.filter(_.hoepaStatus == 2), counts)
      singleFamilyM1 <- purposes(singleFamily, rateSpreadMean)
      singleFamilyM2 <- purposes(singleFamily, rateSpreadMedian)

      manufacturedP1 <- purposes(manufactured.filter(_.rateSpread == "NA"), counts)
      manufacturedP2 <- purposes(manufactured.filter(pricingDataReported), counts)
      manufacturedH1 <- purposes(manufactured.filter(_.hoepaStatus == 1), counts)
      manufacturedH2 <- purposes(manufactured.filter(_.hoepaStatus == 2), counts)
      manufacturedM1 <- purposes(manufactured, rateSpreadMean)
      manufacturedM2 <- purposes(manufactured, rateSpreadMedian)

      year <- yearF
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
       |    $msaLine
       |    "singleFamily": [
       |        {
       |            "characteristic": "Incidence of Pricing",
       |            "pricingInformation": [
       |                {
       |                    "pricing": "No pricing reported",
       |                    "purposes": $singleFamilyP1
       |                },
       |                {
       |                    "pricing": "Pricing reported",
       |                    "purposes": $singleFamilyP2
       |                },
       |                {
       |                    "pricing": "Mean (points above average prime offer rate: only includes loans with APR above the threshold)",
       |                    "purposes": $singleFamilyM2
       |                },
       |                {
       |                    "pricing": "Median (points above the average prime offer rate: only includes loans with APR above the threshold)",
       |                    "purposes": $singleFamilyM2
       |                }
       |            ]
       |        },
       |        {
       |            "characteristic": "HOEPA Status",
       |            "pricingInformation": [
       |                {
       |                    "pricing": "HOEPA loan",
       |                    "purposes": $singleFamilyH1
       |                },
       |                {
       |                    "pricing": "Not a HOEPA loan",
       |                    "purposes": $singleFamilyH2
       |                }
       |            ]
       |        }
       |    ],
       |    "manufactured": [
       |        {
       |            "characteristic": "Incidence of Pricing",
       |            "pricingInformation": [
       |                {
       |                    "pricing": "No pricing reported",
       |                    "purposes": $manufacturedP1
       |                },
       |                {
       |                    "pricing": "Pricing reported",
       |                    "purposes": $manufacturedP2
       |                },
       |                {
       |                    "pricing": "Mean (points above average prime offer rate: only includes loans with APR above the threshold)",
       |                    "purposes": $manufacturedM1
       |                },
       |                {
       |                    "pricing": "Median (points above the average prime offer rate: only includes loans with APR above the threshold)",
       |                    "purposes": $manufacturedM2
       |                }
       |            ]
       |        },
       |        {
       |            "characteristic": "HOEPA Status",
       |            "pricingInformation": [
       |                {
       |                    "pricing": "HOEPA loan",
       |                    "purposes": $manufacturedH1
       |                },
       |                {
       |                    "pricing": "Not a HOEPA loan",
       |                    "purposes": $manufacturedH2
       |                }
       |            ]
       |        }
       |    ]
       |}
       |
     """.stripMargin

      DisclosureReportPayload(metaData.reportTable, fipsCode.toString, report)
    }
  }

  private def purposes[ec: EC, mat: MAT, as: AS](lars: Source[LoanApplicationRegister, NotUsed], collector: Source[LoanApplicationRegister, NotUsed] => Future[String]): Future[String] = {
    for {
      purchase <- lienDisposition("Home Purchase", lars.filter(lar => lar.loan.purpose == 1), collector)
      refinance <- lienDisposition("Refinance", lars.filter(lar => lar.loan.purpose == 3), collector)
      improvement <- lienDisposition("Home Improvement", lars.filter(lar => lar.loan.purpose == 2), collector)
    } yield List(purchase, refinance, improvement).mkString("[", ",", "]")
  }

  private def lienDisposition[ec: EC, mat: MAT, as: AS](title: String, source: Source[LoanApplicationRegister, NotUsed], collector: Source[LoanApplicationRegister, NotUsed] => Future[String]): Future[String] = {
    for {
      firstLien <- collector(source.filter(lar => lar.lienStatus == 1))
      juniorLien <- collector(source.filter(lar => lar.lienStatus == 2))
      noLien <- collector(source.filter(lar => lar.lienStatus != 1 && lar.lienStatus != 2))
    } yield {
      s"""
         |{
         |    "purpose": "$title",
         |    "firstLienCount": $firstLien,
         |    "juniorLienCount": $juniorLien,
         |    "noLienCount": $noLien
         |}
       """
    }
  }

  private def counts[ec: EC, mat: MAT, as: AS](source: Source[LoanApplicationRegister, NotUsed]): Future[String] = count(source).map(_.toString)

  private def rateSpreadMedian[ec: EC, mat: MAT, as: AS](lars: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    val rateSpreadsF: Future[Seq[Double]] =
      lars.filter(pricingDataReported)
        .map(lar => lar.rateSpread.toDouble)
        .runWith(Sink.seq)

    rateSpreadsF.map(seq => if (seq.isEmpty) "\"\"" else calculateMedian(seq).toString)
  }

  private def rateSpreadMean[ec: EC, mat: MAT, as: AS](lars: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    val loansFiltered = lars.filter(pricingDataReported)
    val loanCountF = count(loansFiltered)
    val rateSpreadSumF = sumDouble(loansFiltered, rateSpread)
    for {
      count <- loanCountF
      totalRateSpread <- rateSpreadSumF
    } yield {
      if (count == 0) "\"\""
      else {
        val v = totalRateSpread / count
        BigDecimal(v).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble.toString
      }
    }
  }

}
