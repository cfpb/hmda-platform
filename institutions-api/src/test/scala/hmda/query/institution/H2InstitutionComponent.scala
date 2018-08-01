package hmda.query.institution

import hmda.query.dao.H2Persistence

object H2InstitutionComponent extends InstitutionComponent with H2Persistence
