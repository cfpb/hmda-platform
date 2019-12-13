package hmda.dashboard.services

import hmda.dashboard.models.TotalFilers
import monix.eval.Task

trait QueryService {
  def fetchTotalFilers(year: Int): Task[Vector[TotalFilers]]
}
