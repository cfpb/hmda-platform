package hmda.publisher.helper

import com.typesafe.config.ConfigFactory
import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

trait PrivateAWSConfigLoader {

  val awsConfigPrivate = ConfigFactory.load("application.conf").getConfig("private-aws")
  val accessKeyIdPrivate  = awsConfigPrivate.getString("private-access-key-id")
  val secretAccessPrivate = awsConfigPrivate.getString("private-secret-access-key ")
  val regionPrivate       = awsConfigPrivate.getString("private-region")
  val bucketPrivate       = awsConfigPrivate.getString("private-s3-bucket")
  val environmentPrivate  = awsConfigPrivate.getString("private-environment")

  val awsCredentialsProviderPrivate = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyIdPrivate, secretAccessPrivate))

  val awsRegionProviderPrivate: AwsRegionProvider = () => Region.of(regionPrivate)

}
