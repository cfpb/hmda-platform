package hmda.institution.query

class InstitutionComponentSpec extends InstitutionAsyncSetup {

  "Institution Component" must {
    "find institutions by email" in {
      findByEmail("email@bbb.com", "2018").map { xs =>
        xs.size mustBe 2
        xs.map(i => i.LEI) mustBe Seq("AAA", "BBB")
      }
      findByEmail("email@bbb.com", "2019").map(xs => xs.size mustBe 0)
      findByEmail("email@bbb.com", "2020").map(xs => xs.size mustBe 0)
      findByEmailAnyYear("email@bbb.com").map { xs =>
        xs.size mustBe 2
        xs.map(i => i.LEI) mustBe Seq("AAA", "BBB")
      }
    }

    "find institutions by year" in {
      for {
        results2018 <- findByYear("2018")
        results2019 <- findByYear("2019")
        results2020 <- findByYear("2020")
      } yield {
        results2018.size mustBe 3
        results2019.size mustBe 0
        results2020.size mustBe 0
      }
    }

    "update institutions" in {
      val inst = InstitutionEmailEntity(lei = "BBB", emailDomain = "ccc.com")
      updateEmails(inst).flatMap { t =>
        t mustBe 1
        findByEmail("emailTest@ccc.com", "2018").map { xs =>
          xs.size mustBe 1
          xs.map(i => i.LEI) mustBe Seq("BBB")
        }
      }
    }

    "delete institutions" in {
      findByEmail("email@eee.com", "2018").map { xs =>
        xs.size mustBe 1
        xs.map(i => i.LEI) mustBe Seq("EEE")
      }
      deleteEmails("EEE").map(xs => xs mustBe 1)
      findByEmail("email@eee.com", "2018").map(xs => xs.size mustBe 0)
    }

    "find institutions by field parameters" in {
      findByFields("AAA", "RespA", "taxIdA", "aaa.com", "2018").map { xs =>
        xs.size mustBe 1
        val result = xs.head
        result.LEI mustBe "AAA"
        result.taxId mustBe Some("taxIdA")
        result.respondent.name mustBe Some("RespA")
        result.emailDomains mustBe List("aaa.com")
      }
      findByFields("XXX", "name", "taxId", "xxx.com", "2018").map(xs => xs.size mustBe 0)
    }
  }
}