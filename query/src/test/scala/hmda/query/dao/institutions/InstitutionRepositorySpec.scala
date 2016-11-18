package hmda.query.dao.institutions

import hmda.query.model.institutions.InstitutionEntity
import hmda.query.model.institutions.InstitutionQueryGenerators._
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import slick.driver.H2Driver
import slick.driver.H2Driver.api._
import slick.jdbc.meta.MTable

class InstitutionRepositorySpec extends AsyncWordSpec with MustMatchers with BeforeAndAfterAll with InstitutionRepository with H2Driver {
  override val db: api.Database = Database.forConfig("h2mem")

  override def afterAll(): Unit = {
    super.afterAll()
    db.close()
  }

  val repository = new InstitutionBaseRepository

  "Institution Repository" must {
    val i = createInstitution()
    val modified = i.copy(cra = true)

    "create schema" in {
      val fTables = for {
        s <- repository.createSchema()
        tables <- db.run(MTable.getTables)
      } yield tables

      fTables.map { tables =>
        tables.size mustBe 1
      }
    }

    "save new institution" in {
      val fInsert = repository.save(i)

      fInsert.map { x =>
        x mustBe 1
      }
    }

    "find modified institution" in {
      val fModified = repository.update(modified)

      fModified.map { x =>
        x mustBe 1
      }

      val fCra = for {
        m <- repository.find(modified.id).mapTo[Option[InstitutionEntity]]
      } yield {
        m.getOrElse(InstitutionEntity()).cra
      }

      fCra.flatMap { x =>
        x mustBe true
      }

    }

  }

  private def createInstitution() = {
    institutionQueryGen.sample.get
  }
}
