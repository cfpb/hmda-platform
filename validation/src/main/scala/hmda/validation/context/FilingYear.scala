package hmda.validation.context

import java.time.Year

/**
 * Created by keelerh on 7/28/16.
 */
sealed abstract class FilingYear(val year: Year)
object Filing2016 extends FilingYear(Year.of(2016))
object Filing2017 extends FilingYear(Year.of(2017))

