//package hmda.query.dao.institutions
//
//import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
//import slick.driver.H2Driver
//import slick.driver.H2Driver.api._
//import slick.jdbc.meta.MTable
//import hmda.query.model.institutions.InstitutionQueryGenerators._
//
//class InstitutionDAOSpec extends AsyncWordSpec with MustMatchers with BeforeAndAfterAll with InstitutionDAO with H2Driver {
//
//  var db: Database = _
//  val institutions = TableQuery[Institutions]
//
//  override def beforeAll(): Unit = {
//    super.beforeAll()
//    db = Database.forConfig("h2mem")
//  }
//
//  override def afterAll(): Unit = {
//    super.afterAll()
//    db.close()
//  }
//
//  "Institutions" must {
//
//    val i = createInstitution()
//    val modified = i.copy(cra = true)
//
//    "create schema" in {
//      val fTables = for {
//        s <- db.run(createSchema())
//        tables <- db.run(MTable.getTables)
//      } yield tables
//
//      fTables.map { tables =>
//        tables.size mustBe 1
//      }
//    }
//
//    "save new institution" in {
//      val fInsert = db.run(insertOrUpdate(i))
//
//      fInsert.map { x =>
//        x mustBe 1
//      }
//    }
//
//    "modify institution" in {
//      val fModified = db.run(insertOrUpdate(modified))
//
//      fModified.map { x =>
//        x mustBe 1
//      }
//    }
//
//    "get modified institution" in {
//      val fInst = for {
//        i <- db.run(get(modified.id))
//      } yield i
//
//      fInst.map { x =>
//        x.get mustBe modified
//      }
//
//    }
//  }
//
//  private def createInstitution() = {
//    institutionQueryGen.sample.get
//  }
//
//}
