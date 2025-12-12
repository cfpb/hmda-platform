package hmda.publisher.helper

import com.typesafe.config.ConfigFactory
import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

trait PublicAWSConfigLoader {

  val awsConfigPublic             = ConfigFactory.load("application.conf").getConfig("public-aws")
  val regionPublic                 = awsConfigPublic.getString("public-region")
  val bucketPublic                 = awsConfigPublic.getString("public-s3-bucket")
  val awsRegionProviderPublic: AwsRegionProvider = () => Region.of(regionPublic)

}
