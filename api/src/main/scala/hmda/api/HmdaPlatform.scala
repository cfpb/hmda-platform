package hmda.api

object HmdaPlatform {

  def main(args: Array[String]): Unit = {
    HmdaApi.main(Array.empty)
    new HmdaAdminApi(HmdaApi.system).main(Array.empty)
  }

}
