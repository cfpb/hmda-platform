import InstitutionEntityGenerators._
import hmda.regulator.query.InstitutionEntity

class InstitutionRepositorySpec extends InstitutionAsyncSetup {

  "Institution Repository" must {
    "insert new record" in {
      val i = institutionEntityGen.sample
        .getOrElse(InstitutionEntity())
        .copy(lei = "AAA")
      institutionRepository.insertOrUpdate(i).map(x => x mustBe 1)
    }

    "modify records and read them back" in {
      val i = institutionEntityGen.sample
        .getOrElse(InstitutionEntity())
        .copy(lei = "BBB", agency = 2)
      institutionRepository.insertOrUpdate(i).map(x => x mustBe 1)

      val modified = i.copy(agency = 8)
      institutionRepository.insertOrUpdate(modified).map(x => x mustBe 1)
      institutionRepository.findById(i.lei).map {
        case Some(x) => x.agency mustBe 8
        case None    => fail
      }
    }

    "delete record" in {
      val i = institutionEntityGen.sample
        .getOrElse(InstitutionEntity())
        .copy(lei = "BBB", agency = 2)
      institutionRepository.insertOrUpdate(i).map(x => x mustBe 1)

      institutionRepository.deleteById(i.lei).map(x => x mustBe 1)
      institutionRepository.findById(i.lei).map {
        case Some(_) => fail
        case None    => succeed
      }
    }
  }

}
