package hmda.institution.query

class InstitutionComponentSpec extends InstitutionAsyncSetup {

  "Institution Component" must {
    "find institutions by email" in {
      findByEmail("email@bbb.com", "2018".toArray[String]).map { xs =>
        xs.size mustBe 2
        xs.map(i => i.LEI) mustBe Seq("AAA", "BBB")
      }
    }

    "update institutions" in {
      val inst = InstitutionEmailEntity(lei = "BBB", emailDomain = "ccc.com")
      updateEmails(inst).flatMap { t =>
        t mustBe 1
        findByEmail("emailTest@ccc.com", "2018".toArray[String]).map { xs =>
          xs.size mustBe 1
          xs.map(i => i.LEI) mustBe Seq("BBB")
        }
      }
    }

    "delete institutions" in {
      findByEmail("email@eee.com", "2018".toArray[String]).map { xs =>
        xs.size mustBe 1
        xs.map(i => i.LEI) mustBe Seq("EEE")
      }
      deleteEmails("EEE").map { xs =>
        xs mustBe 1
      }
      findByEmail("email@eee.com", "2018".toArray[String]).map(xs => xs.size mustBe 0)
    }

    "find institutions by field parameters" in {
      findByFields("AAA", "RespA", "taxIdA", "aaa.com", "2018".toArray[String]).map { xs =>
        xs.size mustBe 1
        val result = xs.head
        result.LEI mustBe "AAA"
        result.taxId mustBe Some("taxIdA")
        result.respondent.name mustBe Some("RespA")
        result.emailDomains mustBe List("aaa.com")
      }
      findByFields("XXX", "name", "taxId", "xxx.com", "2018".toArray[String]).map { xs =>
        xs.size mustBe 0
      }
    }
  }
}
