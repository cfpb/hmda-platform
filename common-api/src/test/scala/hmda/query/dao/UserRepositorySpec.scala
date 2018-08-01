package hmda.query.dao

import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpec}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class UserRepositorySpec
    extends WordSpec
    with MustMatchers
    with BeforeAndAfterAll {

  import H2UserComponent._
  import H2UserComponent.profile.api._

  val timeout = 3.seconds

  override def beforeAll = {
    val schema = UserRepository.table.schema
    val f = for {
      _ <- db.run(DBIO.seq(schema.create))
      size <- db.run(UserRepository.table.size.result)
    } yield {
      size match {
        case 0 =>
          db.run(UserRepository.table += UserEntity(None, "User")) map (Some(_))
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

  "User Repository" must {
    "query users" in {
      val query = UserRepository.table
      val usersList = Await.result(db run query.result, timeout)
      usersList must not be empty
      usersList must have length 1
    }

    "find a user by id" in {
      val future = UserRepository.findById(1)
      val userOption = Await.result(future, timeout)
      userOption mustBe Some(UserEntity(Some(1L), "User"))
    }

    "insert a new user" in {
      val user = UserEntity(None, "Test")
      val insertedRows = Await.result(UserRepository.insert(user), timeout)
      insertedRows mustBe 1

      val future = UserRepository.findById(2)
      val userOption = Await.result(future, timeout)
      userOption mustBe Some(UserEntity(Some(2), "Test"))

      Await.result(db run UserRepository.table.size.result, timeout) mustBe 2
    }

    "delete an existing user" in {
      val future = UserRepository.deleteById(1)
      val affectedRows = Await.result(future, timeout)
      affectedRows mustBe 1

      val userFuture = UserRepository.findById(1)
      val userOption = Await.result(userFuture, timeout)
      userOption mustBe None
    }

  }

}
