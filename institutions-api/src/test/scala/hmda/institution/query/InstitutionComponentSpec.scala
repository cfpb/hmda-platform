package hmda.institution.query

class InstitutionComponentSpec extends InstitutionAsyncSetup {

  "Institution Component" must {
    "find institutions by email" in {
      findByEmail("email@bbb.com").map { xs =>
        val institutions = xs._1
        val emails = xs._2
        institutions.size mustBe 2
        institutions.map(_.lei) mustBe Seq("AAA", "BBB")
        emails.size mustBe 2
      }
    }
  }
}
