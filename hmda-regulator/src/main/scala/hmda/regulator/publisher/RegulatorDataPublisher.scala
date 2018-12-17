package hmda.regulator.publisher

import java.time.LocalDateTime
import java.time.LocalDateTime._
import java.time.format.DateTimeFormatter

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.alpakka.s3.impl.ListBucketVersion2
import akka.stream.alpakka.s3.javadsl.S3Client
import akka.stream.alpakka.s3.{MemoryBufferType, S3Settings}
import akka.util.ByteString
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import com.typesafe.config.ConfigFactory
import hmda.model.filing.submission.SubmissionId
import hmda.regulator.data.RegulatorDataEntity
import hmda.query.HmdaQuery._

sealed trait RegulatorDataPublisher
case class UploadToS3(regulatorDataEntity: RegulatorDataEntity)
    extends RegulatorDataPublisher

object RegulatorDataPublisher {

//        case UploadToS3(regulatorDataEntity) =>
//          val now = LocalDateTime.now()
//          val fileName = s"${now.format(DateTimeFormatter.ISO_LOCAL_DATE)}" + "_" + regulatorDataEntity.dataType + ".txt"
//
//          log.info(s"Uploading Regulator Data file : $fileName" + "  to S3.")

//          val s3Sink =
//            s3Client.multipartUpload(bucket,
//                                     s"$environment/regulator+/$year/$fileName")

//          readRawData(regulatorDataEntity)
//            .map(l => l.data)
//            .drop(1)
//            .map(s => regulatorDataEntity.toCSV + "\n")
//            .map(s => ByteString(s))
//            .runWith(s3Sink)

}
