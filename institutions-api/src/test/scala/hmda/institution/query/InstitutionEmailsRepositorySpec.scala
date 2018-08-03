package hmda.institution.query

import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, MustMatchers}
import hmda.query.DbConfiguration._
import slick.dbio.DBIOAction

import scala.concurrent.Await
import scala.concurrent.duration._
import InstitutionEntityGenerators._

class InstitutionEmailsRepositorySpec
    extends AsyncWordSpec
    with MustMatchers
    with BeforeAndAfterAll
    with InstitutionComponent {

  val timeout = 5.seconds

  val institutionRepository = new InstitutionRepository(config)
  val emailRepository = new InstitutionEmailsRepository(config)
  val db = emailRepository.db

  override def beforeAll() = {
    import config.profile.api._
    super.beforeAll()
    val setup = db.run(
      DBIOAction.seq(
        institutionsTable.schema.create,
        institutionsTable ++= Seq(
          institutionEntityGen.sample
            .getOrElse(InstitutionEntity())
            .copy(lei = "AAA"),
          institutionEntityGen.sample
            .getOrElse(InstitutionEntity())
            .copy(lei = "BBB")
        ),
        institutionEmailsTable.schema.create,
        institutionEmailsTable ++= Seq(
          InstitutionEmailEntity(1, "AAA", "aaa.com"),
          InstitutionEmailEntity(2, "AAA", "bbb.com"),
          InstitutionEmailEntity(3, "BBB", "bbb.com")
        )
      ))
    Await.result(setup, timeout)
  }

  override def afterAll() = {
    super.afterAll()
    Await.result(emailRepository.dropSchema(), timeout)
    Await.result(institutionRepository.dropSchema(), timeout)
  }

  "Institution Emails repository" must {
    "Return inital records" in {
      val email1 = "email@aaa.com"
      val email2 = "email@bbb.com"
      emailRepository.findByEmail(email1).map(xs => xs.size mustBe 1)
      emailRepository.findByEmail(email2).map(xs => xs.size mustBe 2)
    }
  }

}
