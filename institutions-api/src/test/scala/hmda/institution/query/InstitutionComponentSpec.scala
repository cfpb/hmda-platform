package hmda.institution.query

class InstitutionComponentSpec extends InstitutionAsyncSetup {

  "Institution Component" must {
    "find institutions by email" in {
      findByEmail("email@bbb.com").map { xs =>
        xs.size mustBe 2
        xs.map(i => i.LEI) mustBe Seq("AAA", "BBB")
      }
    }
    "find institutions by field parameters" in {
      findByFields("AAA", "RespA", "taxIdA", "aaa.com").map { xs =>
        xs.size mustBe 1
        val result = xs.head
        result.LEI mustBe "AAA"
        result.taxId mustBe Some("taxIdA")
        result.respondent.name mustBe Some("RespA")
        result.emailDomains mustBe List("aaa.com")
      }
      findByFields("XXX", "name", "taxId", "xxx.com").map { xs =>
        xs.size mustBe 0
      }
    }
  }
}
