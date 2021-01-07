package hmda.api.http

import com.typesafe.config.ConfigFactory

// $COVERAGE-OFF$
object EmailUtils {
  val config = ConfigFactory.load("reference.conf")
  val publicDomains = config.getString("hmda.email.publicDomains").split(",")

  def checkIfPublicDomain(domain: String): Boolean =
    publicDomains.contains(domain)

  def checkListIfPublicDomain(domains: Seq[String]): Boolean =
    !(domains.intersect(publicDomains).isEmpty)
}
// $COVERAGE-ON$