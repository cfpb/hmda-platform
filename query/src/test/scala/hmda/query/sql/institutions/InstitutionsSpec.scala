package hmda.query.sql.institutions

import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import slick.driver.H2Driver.api._
import slick.jdbc.meta.MTable
import hmda.query.model.institutions.InstitutionQueryGenerators._
import scala.concurrent.ExecutionContext.Implicits.global

class InstitutionsSpec extends AsyncWordSpec with MustMatchers with BeforeAndAfterAll {

  var db: Database = _

  val institutions = TableQuery[Institutions]

  override def beforeAll(): Unit = {
    super.beforeAll()
    db = Database.forConfig("h2mem1")
  }

  override def afterAll(): Unit = {
    super.afterAll()
    db.close()
  }

  "Institutions Query" must {
    "create schema" in {
      val fTables = for {
        s <- createSchema()
        tables <- db.run(MTable.getTables)
      } yield tables

      fTables.map { tables =>
        tables.size mustBe 1
        tables.count(_.name.name.equalsIgnoreCase("institutions")) mustBe 1
      }

    }

    "insert new institution" in {
      val i = getInstitution()
      val fInsert = db.run(institutions += i)

      fInsert.map { x =>
        x mustBe 1
      }
    }
  }

  private def createSchema() = {
    db.run(institutions.schema.create)
  }

  private def getInstitution() = {
    institutionQueryGen.sample.get
    //    db.run(institutions += i)
  }

}
