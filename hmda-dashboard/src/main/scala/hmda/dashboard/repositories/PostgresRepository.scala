package hmda.dashboard.repositories

import monix.eval.Task
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class PostgresRepository (tableName: String, config: DatabaseConfig[JdbcProfile]) {

  import config._
  import config.profile.api._

  def escape(str: String): String = str.replace("'", "")

  def formatSeq(strs: Seq[String]): String =
    strs.map(each => s"\'$each\'").mkString(start = "(", sep = ",", end = ")")

  def eq(fieldName: String, value: String): String =
    s"${escape(fieldName)} = '${escape(value)}'"

  def in(fieldName: String, values: Seq[String]): String =
    s"${escape(fieldName)} IN ${formatSeq(values.map(escape))}"

  def whereAndOpt(expression: String, remainingExpressions: String*): String = {
    val primary = s"WHERE $expression"
    if (remainingExpressions.isEmpty) primary
    else {
      val secondaries =
        remainingExpressions
          //do not include year in the WHERE clause because all entries in the table (modifiedlar2018_snapshot) have filing_year = 2018
          .filterNot(_ == "filing_year IN ('2018')")
          .map(expr => s"AND $expr")
          .mkString(sep = " ")
      s"$primary $secondaries"
    }
  }

  def find() = {
    "hi"
  }

  def healthCheck: Task[Unit] = {
    Task.deferFuture (db.run (sql"SELECT 1".as[Int] ) ).guarantee (Task.shift).void
  }
}
