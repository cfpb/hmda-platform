package hmda.institution.query

import hmda.institution.query.InstitutionEntityGenerators.institutionEntityGen
import hmda.query.DbConfiguration.dbConfig
import slick.dbio.DBIOAction

import scala.concurrent.duration._
import scala.concurrent.Await

trait InstitutionSetup extends InstitutionComponent {

  implicit val institutionRepository = new InstitutionRepository(dbConfig)
  implicit val emailRepository = new InstitutionEmailsRepository(dbConfig)
  val db = emailRepository.db

  val duration = 5.seconds

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

  def setup() = {
    import dbConfig.profile.api._
    val setup = db.run(
      DBIOAction.seq(
        institutionsTable.schema.create,
        institutionsTable ++= Seq(
          instA,
          instB
        ),
        institutionEmailsTable.schema.create,
        institutionEmailsTable ++= Seq(
          InstitutionEmailEntity(1, "AAA", "aaa.com"),
          InstitutionEmailEntity(2, "AAA", "bbb.com"),
          InstitutionEmailEntity(3, "BBB", "bbb.com")
        )
      ))
    Await.result(setup, duration)
  }

  def tearDown() = {
    Await.result(emailRepository.dropSchema(), duration)
    Await.result(institutionRepository.dropSchema(), duration)
  }

}
