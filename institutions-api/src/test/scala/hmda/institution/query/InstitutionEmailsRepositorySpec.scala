package hmda.institution.query

class InstitutionEmailsRepositorySpec extends InstitutionAsyncSetup {

  "Institution Emails repository" must {
    "return inital records" in {
      val email1 = "email@aaa.com"
      val email2 = "email@bbb.com"
      emailRepository.findByEmail(email1).map(xs => xs.size mustBe 1)
      emailRepository.findByEmail(email2).map(xs => xs.size mustBe 2)
    }

    "filter by LEI" in {
      val lei = "AAA"
      emailRepository.findByLei(lei).map { xs =>
        xs.size mustBe 2
      }
    }
  }

}
