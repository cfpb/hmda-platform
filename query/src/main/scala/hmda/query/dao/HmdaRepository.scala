package hmda.query.dao

import scala.reflect._
import slick.driver.JdbcProfile

import scala.concurrent.Future

trait HmdaEntity {
  def id: String
}

trait HmdaRepository {
  profile: JdbcProfile =>

  import profile.api._

  val db: Database

  abstract class HmdaTable[E: ClassTag](tag: Tag, schemaName: Option[String], tableName: String)
      extends Table[E](tag, schemaName, tableName) {
    val classOfEntity = classTag[E].runtimeClass
    val id: Rep[String] = column[String]("Id")
  }

  trait HmdaBaseRepositoryComponent[T <: HmdaTable[E], E <: HmdaEntity] {
    def find(id: String): Future[Option[E]]
    def findAll(): Future[Seq[E]]
    def save(row: E): Future[Int]
    def update(row: E): Future[Int]
    def delete(id: String): Future[Int]
  }

  trait HmdaBaseRepositoryQuery[T <: HmdaTable[E], E <: HmdaEntity] {
    val query: TableQuery[T]

    def createSchemaQuery() = {
      query.schema.create
    }

    def deleteSchemaQuery() = {
      query.schema.drop
    }

    def findByIdQuery(id: String) = {
      query.filter(_.id === id)
    }

    def findAllQuery() = {
      query
    }

    def insertOrUpdateQuery(e: E) = {
      query.insertOrUpdate(e)
    }

    def updateQuery(e: E) = {
      query.update(e)
    }

    def deleteQuery(id: String) = {
      findByIdQuery(id).delete
    }

  }

  abstract class HmdaBaseRepository[T <: HmdaTable[E], E <: HmdaEntity: ClassTag](clazz: TableQuery[T])
      extends HmdaBaseRepositoryQuery[T, E] with HmdaBaseRepositoryComponent[T, E] {

    val clazzTable: TableQuery[T] = clazz
    lazy val clazzEntity = classTag[E].runtimeClass
    val query: TableQuery[T] = clazz

    def createSchema() = {
      db.run(createSchemaQuery())
    }

    def dropSchema() = {
      db.run(deleteSchemaQuery())
    }

    def find(id: String) = {
      db.run(findByIdQuery(id).result.headOption)
    }

    def findAll() = {
      db.run(findAllQuery().result)
    }

    def save(e: E) = {
      db.run(insertOrUpdateQuery(e))
    }

    def update(e: E) = {
      db.run(updateQuery(e))
    }

    def delete(id: String) = {
      db.run(deleteQuery(id))
    }

  }

}

