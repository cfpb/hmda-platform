package hmda.query.institution

import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpec}

import InstitutionEntityGenerators._

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class InstitutionRepositorySpec
    extends WordSpec
    with MustMatchers
    with BeforeAndAfterAll {

  import H2InstitutionComponent._
  import H2InstitutionComponent.profile.api._

  val timeout = 3.seconds

  val initialInstitutionEntity =
    institutionEntityGen.sample
      .getOrElse(InstitutionEntity())
      .copy(lei = Some("AAA"))

  override def beforeAll = {
    val schema = InstitutionRepository.table.schema
    val f = for {
      _ <- db.run(DBIO.seq(schema.create))
      size <- db.run(InstitutionRepository.table.size.result)
    } yield {
      size match {
        case 0 =>
          db.run(InstitutionRepository.table += initialInstitutionEntity)
            .map(Some(_))
        case _ =>
          Future {
            None
          }
      }
    }

    Await.result(f, timeout)
  }

  override def afterAll = {
    db.close()
  }

  "Institution Repository" must {
    "query institution" in {
      val query = InstitutionRepository.table
      val institutionList = Await.result(db.run(query.result), timeout)
      institutionList must not be empty
      institutionList must have length 1
    }

    "find an institution by LEI" in {
      val future = InstitutionRepository.findById(
        initialInstitutionEntity.lei.getOrElse(""))
      val institutionOption = Await.result(future, timeout)
      institutionOption mustBe Some(initialInstitutionEntity)
    }

    "insert a new institution" in {
      val institution = institutionEntityGen.sample
        .getOrElse(InstitutionEntity())
        .copy(lei = Some("BBB"))
      val inserterRows =
        Await.result(InstitutionRepository.insert(institution), timeout)
      inserterRows mustBe 1

      val future = InstitutionRepository.findById("BBB")
      val institutionOption = Await.result(future, timeout)
      institutionOption mustBe Some(institution)
    }

    "delete an existing institution" in {
      val future = InstitutionRepository.deleteById("BBB")
      val affectedRows = Await.result(future, timeout)
      affectedRows mustBe 1

      val institutionFuture = InstitutionRepository.findById("BBB")
      val institutionOption = Await.result(institutionFuture, timeout)
      institutionOption mustBe None
    }
  }

}
