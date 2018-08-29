package hmda.institution.query

class InstitutionComponentSpec extends InstitutionAsyncSetup {

  "Institution Component" must {
    "find institutions by email" in {
      findByEmail("email@bbb.com").map { xs =>
        xs.size mustBe 2
        xs.map(i => i.LEI) mustBe Seq("AAA", "BBB")
      }
    }
  }
}
