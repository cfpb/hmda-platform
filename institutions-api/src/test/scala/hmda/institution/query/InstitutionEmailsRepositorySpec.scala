package hmda.institution.query

import scala.concurrent.Future

class InstitutionEmailsRepositorySpec extends InstitutionAsyncSetup {

  "Institution Emails repository" must {
    "return initial records" in {
      val email1 = "email@aaa.com"
      val email2 = "email@bbb.com"
      emailRepository.getId(emailRepository.table.baseTableRow)
      emailRepository.findByEmail(email1).map(xs => xs.size mustBe 1)
      emailRepository.findByEmail(email2).map(xs => xs.size mustBe 2)
    }

    "filter by LEI" in {
      val lei = "AAA"
      emailRepository.findByLei(lei).map(xs => xs.size mustBe 2)
    }

    "delete by LEI" in {
      emailRepository
        .findByLei("AAA")
        .flatMap(emails => Future.traverse(emails.map(_.id))(emailRepository.deleteById))
        .map(xs => xs.length mustBe 2)
    }
  }

}