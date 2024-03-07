
package hmda.institution.projection

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.testkit.TestKit
import hmda.institution.query.InstitutionEmailComponent
import hmda.messages.institution.InstitutionEvents.{ InstitutionCreated, InstitutionDeleted, InstitutionModified }
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionGenerators._
import hmda.query.DbConfiguration.dbConfig
import org.scalatest.concurrent.{ Eventually, ScalaFutures }
import org.scalatest.time.{ Millis, Minute, Span }
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpecLike }

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{ Future }

class InstitutionDBProjectionSpec
  extends TestKit(ActorSystem("institutiondb-spec"))
    with WordSpecLike
    with MustMatchers
    with ScalaFutures
    with Eventually
    with InstitutionEmailComponent
    with BeforeAndAfterAll {

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(1, Minute), interval = Span(100, Millis))

  val emailRepository           = new InstitutionEmailsRepository(dbConfig)

  override def beforeAll(): Unit = {
    val futures = Future.sequence(institutionRepositories.values.map(_.createSchema()).toSeq ++
      tsRepositories.values.map(_.createSchema()).toSeq :+
      emailRepository.createSchema())
    whenReady(futures)(_ => ())
  }

  override def afterAll(): Unit = {
    val futures = Future.sequence(
      institutionRepositories.values.map(_.dropSchema()).toSeq ++
        tsRepositories.values.map(_.dropSchema()) :+
        emailRepository.dropSchema()
    )
    whenReady(futures)(_ => ())
  }

  "InstitutionDBProjection stores, modifies and deletes events" in {
    val actor                               = system.spawn(InstitutionDBProjection.behavior, InstitutionDBProjection.name)
    lazy val sampleInstitution: Institution = institutionGen.sample.getOrElse(sampleInstitution)

    List(2018, 2019, 2020).foreach { year =>
      actor ! ProjectEvent(InstitutionCreated(sampleInstitution.copy(activityYear = year, emailDomains = List("custom@bbb.com"))))
      actor ! ProjectEvent(InstitutionModified(sampleInstitution.copy(activityYear = year)))
      actor ! ProjectEvent(InstitutionDeleted(sampleInstitution.LEI, year))
    }

    eventually {
      import dbConfig.profile.api._
      whenReady(
        dbConfig.db.run(
          institutionEmailsTable
            .filter(_.emailDomain === "custom@bbb.com")
            .result
        )
      )(_ must not be empty)
    }
  }
}