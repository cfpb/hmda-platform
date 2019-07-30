package hmda.institution.query

import InstitutionEntityGenerators._

class InstitutionRepositorySpec extends InstitutionAsyncSetup {

  "Institution Repository" must {
    "insert new record" in {
      val i = institutionEntityGen.sample
        .getOrElse(InstitutionEntity())
        .copy(lei = "AAA")
      institutionRepository2018.insertOrUpdate(i).map(x => x mustBe 1)
    }

    "modify records and read them back" in {
      val i = institutionEntityGen.sample
        .getOrElse(InstitutionEntity())
        .copy(lei = "BBB", agency = 2)
      institutionRepository2018.insertOrUpdate(i).map(x => x mustBe 1)

      val modified = i.copy(agency = 8)
      institutionRepository2018.insertOrUpdate(modified).map(x => x mustBe 1)
      institutionRepository2018.findById(i.lei).map {
        case Some(x) => x.agency mustBe 8
        case None    => fail
      }
    }

    "delete record" in {
      val i = institutionEntityGen.sample
        .getOrElse(InstitutionEntity())
        .copy(lei = "BBB", agency = 2)
      institutionRepository2018.insertOrUpdate(i).map(x => x mustBe 1)

      institutionRepository2018.deleteById(i.lei).map(x => x mustBe 1)
      institutionRepository2018.findById(i.lei).map {
        case Some(_) => fail
        case None    => succeed
      }
    }
  }

}
