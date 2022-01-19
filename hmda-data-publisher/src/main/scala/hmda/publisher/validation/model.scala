package hmda.publisher.validation

import slick.lifted.{ AbstractTable, Query, Rep }

// abstraction for transmittal sheet tables to use in validation checks. Abstracts over years
trait TsData {
  type Entry
  type Table <: AbstractTable[Entry]
  val query: Query[Table, Table#TableElementType, Seq]
  val getLei: Table => Rep[String]
  val getTotalLines: Table => Rep[Int]
  val getSubmissionId: Table => Rep[Option[String]]
}
object TsData {
  def apply[Entry_, Table_ <: AbstractTable[Entry_]](
                                                      query_ : Query[Table_, Table_ #TableElementType, Seq]
                                                    )(getLei_ : Table_ => Rep[String], getTotalLines_ : Table_ => Rep[Int], getSubmissionId_ : Table_ => Rep[Option[String]]): TsData =
    new TsData {
      override type Entry = Entry_
      override type Table = Table_
      override val query: Query[Table, Table#TableElementType, Seq] = query_
      override val getLei: Table_ => Rep[String]                    = getLei_
      override val getTotalLines: Table_ => Rep[Int]                = getTotalLines_
      override val getSubmissionId: Table => Rep[Option[String]]    = getSubmissionId_
    }
}

// abstraction for lar tables to use in validation checks. Abstracts over years and modified/non-modifed lars
trait LarData {
  type Entry
  type Table <: AbstractTable[Entry]
  val query: Query[Table, Table#TableElementType, Seq]
  val getLei: Table => Rep[String]
}

object LarData {
  def apply[Entry_, Table_ <: AbstractTable[Entry_]](
                                                      query_ : Query[Table_, Table_ #TableElementType, Seq]
                                                    )(getLei_ : Table_ => Rep[String]): LarData =
    new LarData {
      override type Entry = Entry_
      override type Table = Table_
      override val query: Query[Table, Table#TableElementType, Seq] = query_
      override val getLei: Table_ => Rep[String]                    = getLei_
    }
}

trait PanelData {
  type Entry
  type Table <: AbstractTable[Entry]
  val query: Query[Table, Table#TableElementType, Seq]
  val getLei: Table => Rep[String]
  val getHmdaFiler: Table => Rep[Boolean]
}

object PanelData {
  def apply[Entry_, Table_ <: AbstractTable[Entry_]](
                                                      query_ : Query[Table_, Table_ #TableElementType, Seq]
                                                    )(getLei_ : Table_ => Rep[String],getHmdaFiler_ : Table_ => Rep[Boolean]): PanelData =
    new PanelData {
      override type Entry = Entry_
      override type Table = Table_
      override val query: Query[Table, Table#TableElementType, Seq] = query_
      override val getLei: Table_ => Rep[String]                    = getLei_
      override val getHmdaFiler: Table_ => Rep[Boolean]             = getHmdaFiler_
    }
}