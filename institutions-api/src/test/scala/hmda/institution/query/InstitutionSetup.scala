package hmda.institution.query

import akka.http.scaladsl.testkit.RouteTestTimeout
import hmda.institution.query.InstitutionEntityGenerators.institutionEntityGen
import hmda.query.DbConfiguration.dbConfig
import slick.dbio.DBIOAction

import scala.concurrent.duration._
import scala.concurrent.Await

trait InstitutionSetup extends InstitutionEmailComponent {

  implicit val institutionRepository2018 =
    new InstitutionRepository2018(dbConfig, "institutions2018")
  implicit val institutionRepository2019 =
    new InstitutionRepository2019(dbConfig, "institutions2019")
  implicit val institutionRepository2020 =
    new InstitutionRepository2020(dbConfig, "institutions2020")
  implicit val emailRepository = new InstitutionEmailsRepository(dbConfig)
  val db                       = emailRepository.db

  val duration = 15.seconds
  implicit val routeTimeout = RouteTestTimeout(3.seconds)

  val instA = institutionEntityGen.sample
    .getOrElse(InstitutionEntity())
    .copy(lei = "AAA")
    .copy(respondentName = "RespA")
    .copy(taxId = "taxIdA")

  val instB = institutionEntityGen.sample
    .getOrElse(InstitutionEntity())
    .copy(lei = "BBB")
    .copy(respondentName = "RespB")
    .copy(taxId = "taxIdB")

  val instE = institutionEntityGen.sample
    .getOrElse(InstitutionEntity())
    .copy(lei = "EEE")
    .copy(respondentName = "RespE")
    .copy(taxId = "taxIdE")

  def setup() = {
    import dbConfig.profile.api._
    val setup = db.run(
      DBIOAction.seq(
        institutionsTable2018.schema.create,
        institutionsTable2019.schema.create,
        institutionsTable2020.schema.create,
        institutionsTable2018 ++= Seq(
          instA,
          instB,
          instE
        ),
        institutionEmailsTable.schema.create,
        institutionEmailsTable ++= Seq(
          InstitutionEmailEntity(1, "AAA", "aaa.com"),
          InstitutionEmailEntity(2, "AAA", "bbb.com"),
          InstitutionEmailEntity(3, "BBB", "bbb.com"),
          InstitutionEmailEntity(4, "EEE", "eee.com")
        )
      )
    )
    Await.result(setup, duration)
  }

  def tearDown() = {
    Await.result(emailRepository.dropSchema(), duration)
    Await.result(institutionRepository2018.dropSchema(), duration)
    Await.result(institutionRepository2019.dropSchema(), duration)
    Await.result(institutionRepository2020.dropSchema(), duration)
  }

}
