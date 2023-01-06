package hmda.institution.query

import akka.http.scaladsl.testkit.RouteTestTimeout
import hmda.institution.query.InstitutionEntityGenerators.institutionEntityGen
import hmda.query.DbConfiguration.dbConfig
import slick.dbio.DBIOAction

import scala.concurrent.duration._
import scala.concurrent.Await

trait InstitutionSetup extends InstitutionEmailComponent {

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
        Seq(
          institutionEmailsTable.schema.create,
          institutionEmailsTable ++= Seq(
            InstitutionEmailEntity(1, "AAA", "aaa.com"),
            InstitutionEmailEntity(2, "AAA", "bbb.com"),
            InstitutionEmailEntity(3, "BBB", "bbb.com"),
            InstitutionEmailEntity(4, "EEE", "eee.com")
        )) ++
        tsRepositories.values.map(_.table.schema.create).toSeq ++
        institutionRepositories.values.map(_.table.schema.create).toSeq ++
        Seq(institutionRepositories
          .getOrElse("2018", new InstitutionRepository(dbConfig, "institutions2018"))
          .table ++= Seq(instA, instB, instE)) : _*
      )
    )
    Await.result(setup, duration)
  }

  def tearDown() = {
    Await.result(emailRepository.dropSchema(), duration)
    institutionRepositories.values.foreach{ repo =>
      Await.result(repo.dropSchema(), duration)
    }

    tsRepositories.values.foreach{ repo =>
      Await.result(repo.dropSchema(), duration)
    }

  }

}
