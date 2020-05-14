package hmda.institution.projection

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.testkit.TestKit
import hmda.institution.query.{ InstitutionComponent2018, InstitutionComponent2019, InstitutionComponent2020, InstitutionEmailComponent }
import hmda.messages.institution.InstitutionEvents.{ InstitutionCreated, InstitutionDeleted, InstitutionModified }
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionGenerators._
import hmda.query.DbConfiguration.dbConfig
import org.scalatest.concurrent.{ Eventually, ScalaFutures }
import org.scalatest.time.{ Millis, Minute, Span }
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpecLike }

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

class InstitutionDBProjectionSpec
  extends TestKit(ActorSystem("institutiondb-spec"))
    with WordSpecLike
    with MustMatchers
    with ScalaFutures
    with Eventually
    with InstitutionComponent2018
    with InstitutionComponent2019
    with InstitutionComponent2020
    with InstitutionEmailComponent
    with BeforeAndAfterAll {

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(1, Minute), interval = Span(100, Millis))

  val institutionRepository2018 = new InstitutionRepository2018(dbConfig, "institutions2018")
  val institutionRepository2019 = new InstitutionRepository2019(dbConfig, "institutions2019")
  val institutionRepository2020 = new InstitutionRepository2020(dbConfig, "institutions2020")
  val emailRepository           = new InstitutionEmailsRepository(dbConfig)

  override def beforeAll(): Unit =
    whenReady(
      Future.sequence(
        List(
          institutionRepository2018.createSchema(),
          institutionRepository2019.createSchema(),
          institutionRepository2020.createSchema(),
          emailRepository.createSchema()
        )
      )
    )(_ => ())

  override def afterAll(): Unit =
    whenReady(
      Future.sequence(
        List(
          institutionRepository2018.dropSchema(),
          institutionRepository2019.dropSchema(),
          institutionRepository2020.dropSchema(),
          emailRepository.dropSchema()
        )
      )
    )(_ => ())

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