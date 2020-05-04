package hmda.publication.lar

import akka.actor.typed.ActorSystem
import hmda.publication.lar.EmailGuardian.GuardianProtocol

object EmailReceiptApp {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[GuardianProtocol] = ActorSystem[GuardianProtocol](EmailGuardian(), "email-receipt-app")
    val log                                            = system.log
    log.info("""
               | _   _ ___  ________  ___    _____                _ _
               || | | ||  \/  |  _  \/ _ \  |  ___|              (_) |
               || |_| || .  . | | | / /_\ \ | |__ _ __ ___   __ _ _| |
               ||  _  || |\/| | | | |  _  | |  __| '_ ` _ \ / _` | | |
               || | | || |  | | |/ /| | | | | |__| | | | | | (_| | | |
               |\_| |_/\_|  |_/___/ \_| |_/ \____/_| |_| |_|\__,_|_|_|
               |
               |""".stripMargin)
  }
}