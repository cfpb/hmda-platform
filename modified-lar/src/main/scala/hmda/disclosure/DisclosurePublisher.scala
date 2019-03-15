package hmda.disclosure

import akka.Done
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.stream.alpakka.s3.scaladsl.S3Client
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.util.ByteString
import hmda.model.institution.MsaMd
import hmda.model.modifiedlar._
import hmda.publication.lar.publication.ModifiedLarPublisher.{
  bucket,
  environment,
  year
}
import hmda.query.repository.DisclosureRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import io.circe.generic.auto._
import io.circe.syntax._
import hmda.disclosure._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.model.disclosure._
import hmda.model.filing.EditDescriptionLookup

import scala.concurrent.{ExecutionContext, Future}

object DisclosurePublisher {

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("db")
  val disclosreRepo =
    new DisclosureRepository("modifiedlar2018", databaseConfig)
  val dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm aa")

  implicit val ec = ExecutionContext.global

  def futTracts(lei: String, msaMd: MsaMd) = {
    for {
      tracts <- disclosreRepo.tractsForMsaMd(lei, msaMd.id, 2018)
    } yield {
      (tracts, lei, msaMd)
    }
  }

  def disclosureJsonReport(s3Client: S3Client,
                           leiName: Option[String],
                           actionTaken: Map[String, String],
                           jsonName: String)(implicit actorSystem: ActorSystem,
                                             materializer: ActorMaterializer,
                                             executionContext: ExecutionContext)
    : Sink[ModifiedLoanApplicationRegister, Future[Done]] = {

    Flow[ModifiedLoanApplicationRegister]
      .map(mlar => mlar.lei)
      .mapAsync(4) { lei =>
        disclosureTable(lei, actionTaken)
      }
      .map { disclosure: Seq[(String, Msa, Vector[Tract])] =>
        disclosure.map { d: (String, Msa, Vector[Tract]) =>
          val fatJson = Disclosure(
            d._1,
            leiName,
            "1",
            "Disclosure",
            "Disposition of loan applications, by location of property and type of loan",
            2018,
            dateFormat.format(new java.util.Date()),
            d._2,
            d._3
          )
          val json = fatJson.asJson.noSpaces
          val s3Sink = s3Client.multipartUpload(
            bucket,
            s"$environment/reports/disclosure/$year/${d._1}/${d._2.id}/${jsonName}.json")
          Source
            .single(json)
            .map(ByteString(_))
            .toMat(s3Sink)(Keep.right)
            .run()
        }
      }
      .toMat(Sink.ignore)(Keep.right)
  }

  def disclosureTable(lei: String, actionsTaken: Map[String, String]) = {
    for {
      msaMds <- disclosreRepo.msaMdsByLei(lei, 2018)
      res: Seq[(Vector[String], String, MsaMd)] <- Future.sequence(
        msaMds.map(msaMd => futTracts(lei, msaMd)))
      res2 <- Future.sequence(res.map {
        case (tracts, lei, msamd) =>
          val futTracts: Future[Vector[Tract]] =
            Future.sequence(tracts.map {
              tract: String =>
                //TODO: How do we handle NA in tract?
                val t = TractDisclosure(tract.substring(0, 2).toInt,
                                        tract.substring(2, 5).toInt,
                                        tract.substring(5, 11))
                val futDispositions: Future[Iterable[Disposition]] =
                  Future.sequence(actionsTaken.map { actionTaken =>
                    disposition(lei, msamd, t.tract, 2018, actionTaken)
                  })
                futDispositions.map { dispositions =>
                  val county = EditDescriptionLookup.lookupCounty(t.stateCode,
                                                                  t.countyCode)
                  Tract(s"${county.stateName}/${county.countyName}/${t.tract}",
                        dispositions.toSeq)
                } //TODO: Get the tract name here
            })
          futTracts.map { tracts =>
            val msa = Msa(msamd.id, msamd.name, "State", "StateName")
            (lei, msa, tracts)
          }
      })
    } yield res2
  }

  def disposition(lei: String,
                  msaMd: MsaMd,
                  tract: String,
                  filingYear: Int,
                  actionTaken: (String, String)): Future[Disposition] = {
    for {
      dispositionA <- disclosreRepo.dispositionA(lei,
                                                 msaMd.id,
                                                 tract,
                                                 filingYear,
                                                 actionTaken._2)
      dispositionB <- disclosreRepo.dispositionB(lei,
                                                 msaMd.id,
                                                 tract,
                                                 filingYear,
                                                 actionTaken._2)
      dispositionC <- disclosreRepo.dispositionC(lei,
                                                 msaMd.id,
                                                 tract,
                                                 filingYear,
                                                 actionTaken._2)
      dispositionD <- disclosreRepo.dispositionD(lei,
                                                 msaMd.id,
                                                 tract,
                                                 filingYear,
                                                 actionTaken._2)
      dispositionE <- disclosreRepo.dispositionE(lei,
                                                 msaMd.id,
                                                 tract,
                                                 filingYear,
                                                 actionTaken._2)
      dispositionF <- disclosreRepo.dispositionF(lei,
                                                 msaMd.id,
                                                 tract,
                                                 filingYear,
                                                 actionTaken._2)
      dispositionG <- disclosreRepo.dispositionG(lei,
                                                 msaMd.id,
                                                 tract,
                                                 filingYear,
                                                 actionTaken._2)
    } yield {
      Disposition(actionTaken._1,
                  Seq(
                    dispositionA,
                    dispositionB,
                    dispositionC,
                    dispositionD,
                    dispositionE,
                    dispositionF,
                    dispositionG
                  ))
    }
  }
}
