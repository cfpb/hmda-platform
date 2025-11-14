package hmda.publisher.helper

import com.typesafe.config.ConfigFactory
import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

trait PrivateAWSConfigLoader {

  val awsConfigPrivate = ConfigFactory.load("application.conf").getConfig("private-aws")
  val regionPrivate       = awsConfigPrivate.getString("private-region")
  val bucketPrivate       = awsConfigPrivate.getString("private-s3-bucket")

  val awsRegionProviderPrivate: AwsRegionProvider = () => Region.of(regionPrivate)

}
