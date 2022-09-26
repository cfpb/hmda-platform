package hmda.institution.query

import InstitutionEntityGenerators._

class InstitutionRepositorySpec extends InstitutionAsyncSetup {

  "Institution Repository" must {
    "insert new record" in {
      val i = institutionEntityGen.sample
        .getOrElse(InstitutionEntity())
        .copy(lei = "AAA")
      val (_, repo) = institutionRepositories.head
      repo.insertOrUpdate(i).map(x => x mustBe 1)
    }

    "modify records and read them back" in {
      val i = institutionEntityGen.sample
        .getOrElse(InstitutionEntity())
        .copy(lei = "BBB", agency = 2)
      val (_, repo) = institutionRepositories.head
      repo.insertOrUpdate(i).map(x => x mustBe 1)

      val modified = i.copy(agency = 8)
      repo.insertOrUpdate(modified).map(x => x mustBe 1)
      repo.findById(i.lei).map {
        case Some(x) => x.agency mustBe 8
        case None    => fail
      }
    }

    "delete record" in {
      val i = institutionEntityGen.sample
        .getOrElse(InstitutionEntity())
        .copy(lei = "BBB", agency = 2)
      val (_, repo) = institutionRepositories.head
      repo.insertOrUpdate(i).map(x => x mustBe 1)

      repo.deleteById(i.lei).map(x => x mustBe 1)
      repo.findById(i.lei).map {
        case Some(_) => fail
        case None    => succeed
      }
    }
  }

}
