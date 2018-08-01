package hmda.query.dao

import slick.jdbc.JdbcBackend.Database
import slick.ast.BaseTypedType
import slick.jdbc.JdbcProfile
import slick.lifted.AbstractTable

abstract class Repository[A <: AbstractTable[_], B: BaseTypedType](
    val profile: JdbcProfile,
    db: Database) {

  import profile.api._

  type Id = B
  def table: TableQuery[A]

  def getId(row: A): Rep[Id]

  def filterById(id: Id) = table.filter(getId(_) === id)
  def findById(id: Id) = db.run(filterById(id).result.headOption)

  def insert(model: A#TableElementType) = db.run(table += model)

  def deleteById(id: Id) = {

    val deleteAction = buildDeleteAction(id)
    db run deleteAction.delete
  }

  private def buildDeleteAction(id: Id) = {
    profile.createDeleteActionExtensionMethods(
      profile.deleteCompiler.run(filterById(id).toNode).tree,
      ()
    )
  }
}
