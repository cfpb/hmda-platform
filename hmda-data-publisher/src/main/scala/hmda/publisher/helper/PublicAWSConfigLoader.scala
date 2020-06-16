package hmda.publisher.helper

import com.typesafe.config.ConfigFactory

trait PublicAWSConfigLoader {

  val awsConfigPublic             = ConfigFactory.load("application.conf").getConfig("public-aws")
  val accessKeyIdPublic           = awsConfigPublic.getString("public-access-key-id")
  val secretAccessPublic           = awsConfigPublic.getString("public-secret-access-key ")
  val regionPublic                 = awsConfigPublic.getString("public-region")
  val bucketPublic                 = awsConfigPublic.getString("public-s3-bucket")
  val environmentPublic            = awsConfigPublic.getString("public-environment")
  val awsCredentialsProviderPublic = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyIdPublic, secretAccessPublic))

  val awsRegionProviderPublic: AwsRegionProvider = () => Region.of(regionPublic)

}
