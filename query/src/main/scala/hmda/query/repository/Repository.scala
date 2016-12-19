package hmda.query.repository

import hmda.query.Db
import slick.ast.BaseTypedType
import slick.lifted.AbstractTable

abstract class Repository[T <: AbstractTable[_], I: BaseTypedType] extends Db {
  import config.profile.api._

  type Id = I
  def table: TableQuery[T]

  def getId(row: T): Rep[Id]

  def filterById(id: Id) = table.filter(getId(_) === id)
  def findById(id: Id) = db.run(filterById(id).result.headOption)

  def insertOrUpdate(row: T#TableElementType) = db.run(table.insertOrUpdate(row))
  def update(row: T#TableElementType) = db.run(table.update(row))

}

