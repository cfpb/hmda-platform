package hmda.dataBrowser.repositories

import hmda.dataBrowser.models.{ FilerInstitutionResponse, QueryField, Statistic }
import monix.eval.Task

trait Cache {
  def find(queryFields: List[QueryField]): Task[Option[Statistic]]

  def find(year: Int): Task[Option[FilerInstitutionResponse]]

  def update(queryFields: List[QueryField], statistic: Statistic): Task[Statistic]

  def update(year: Int, filerInstitutionResponse: FilerInstitutionResponse): Task[FilerInstitutionResponse]

  def invalidate(queryField: List[QueryField]): Task[Unit]
}
