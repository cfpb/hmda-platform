package hmda.institution.query

import hmda.query.DbConfiguration._
import InstitutionEntityGenerators._
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, MustMatchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class InstitutionRepositorySpec
    extends AsyncWordSpec
    with MustMatchers
    with BeforeAndAfterAll
    with InstitutionComponent {

  val timeout = 5.seconds

  val repository = new InstitutionRepository(config)

  override def beforeAll = {
    super.beforeAll()
    Await.result(repository.createSchema(), timeout)
  }

  override def afterAll = {
    super.afterAll()
    Await.result(repository.dropSchema(), timeout)
  }

  "Institution Repository" must {
    "insert new record" in {
      val i = institutionEntityGen.sample
        .getOrElse(InstitutionEntity())
        .copy(lei = "AAA")
      repository.insertOrUpdate(i).map(x => x mustBe 1)
    }

    "modify records and read them back" in {
      val i = institutionEntityGen.sample
        .getOrElse(InstitutionEntity())
        .copy(lei = "BBB", agency = 2)
      repository.insertOrUpdate(i).map(x => x mustBe 1)
      val modified = i.copy(agency = 8)
      repository.insertOrUpdate(modified).map(x => x mustBe 1)
      repository.findById(i.lei).map {
        case Some(x) => x.agency mustBe 8
        case None    => fail
      }
    }

    "delete record" in {
      val i = institutionEntityGen.sample
        .getOrElse(InstitutionEntity())
        .copy(lei = "BBB", agency = 2)
      repository.insertOrUpdate(i).map(x => x mustBe 1)

      repository.deleteById(i.lei).map(x => x mustBe 1)
      repository.findById(i.lei).map {
        case Some(_) => fail
        case None    => succeed
      }
    }
  }

}
