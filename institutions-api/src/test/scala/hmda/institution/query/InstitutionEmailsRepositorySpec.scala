package hmda.institution.query

import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, MustMatchers}
import hmda.query.DbConfiguration._
import slick.dbio.DBIOAction

import scala.concurrent.Await
import scala.concurrent.duration._

class InstitutionEmailsRepositorySpec
    extends AsyncWordSpec
    with MustMatchers
    with BeforeAndAfterAll
    with InstitutionComponent {

  val timeout = 5.seconds

  val emailRepository = new InstitutionEmailsRepository(config)
  val db = emailRepository.db
  val emails = emailRepository.table

  override def beforeAll() = {
    import config.profile.api._
    super.beforeAll()
    val setup = db.run(
      DBIOAction.seq(
        emails.schema.create,
        emails ++= Seq(
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
