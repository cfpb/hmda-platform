package hmda.persistence.institutions

import akka.testkit.typed.scaladsl.{BehaviorTestKit, TestInbox}
import com.typesafe.config.ConfigFactory
import hmda.persistence.institutions.InstitutionPersistence.{
  CreateInstitution,
  InstitutionCreated,
  InstitutionEvent
}
import org.scalatest.{BeforeAndAfterAll, MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import hmda.model.institutions.InstitutionGenerators._
import hmda.persistence.util.CassandraUtil

class InstitutionPersistenceSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers
    with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    println("Starting Cassandra")
    val config = ConfigFactory.load()
    println(config.toString)
    CassandraUtil.startEmbeddedCassandra()
  }

  val sampleInstitution = institutionGen.sample.get

  property("An institution can be persisted") {
    val testkit = BehaviorTestKit(InstitutionPersistence.behavior("ABCD12345"))
    println(sampleInstitution.toString)
    //    val inbox = TestInbox[InstitutionEvent]()
    //    testkit.run(CreateInstitution(sampleInstitution, inbox.ref))
    //    inbox.expectMessage(InstitutionCreated(sampleInstitution))
  }

  property("An institution can be modified") {
    pending
  }

  property("An institution can be deleted") {
    pending
  }

}
