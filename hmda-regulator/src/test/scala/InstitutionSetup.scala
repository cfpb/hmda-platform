import hmda.query.DbConfiguration.dbConfig
import slick.dbio.DBIOAction

import scala.concurrent.Await
import scala.concurrent.duration._

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

  val instE = institutionEntityGen.sample
    .getOrElse(InstitutionEntity())
    .copy(lei = "EEE")
    .copy(respondentName = "RespE")
    .copy(taxId = "taxIdE")

  def setup() = {
    val setup = db.run(
      DBIOAction.seq(
        institutionsTable.schema.create,
        institutionsTable ++= Seq(
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
      ))
    Await.result(setup, duration)
  }

  def tearDown() = {
    Await.result(emailRepository.dropSchema(), duration)
    Await.result(institutionRepository.dropSchema(), duration)
  }

}
