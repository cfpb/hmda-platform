package hmda.analytics.query

import hmda.model.filing.lar.LarGenerators._
import hmda.utils.EmbeddedPostgres
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}

class LarComponentSpec extends AsyncWordSpec with LarComponent with EmbeddedPostgres with MustMatchers with BeforeAndAfter {

  "LarComponent" must {
    "be able to persist and delete a 2019 LAR" in {
      val repo      = new LarRepository(dbConfig, "loanapplicationregister2019") // as per hmda.sql
      val sampleLar = larGen.map(LarConverter(_, year = 2019)).sample.get

      repo
        .insert(sampleLar)
        .map(_ mustBe 1)
        .flatMap(_ => repo.deleteByLei(sampleLar.lei))
        .map(_ mustBe 1)
    }

    "be able to persist and delete a quarterly 2019 LAR" in {
      val repo      = new LarRepository(dbConfig, "loanapplicationregister2019") // as per hmda.sql
      val sampleLar = larGen.map(LarConverter(_, year = 2019)).sample.get.copy(isQuarterly = true)

      repo
        .insert(sampleLar)
        .map(_ mustBe 1)
        .flatMap(_ => repo.deletebyLeiAndQuarter(sampleLar.lei))
        .map(_ mustBe 1)
    }
  }

  override def bootstrapSqlFile: String = "hmda.sql"
}