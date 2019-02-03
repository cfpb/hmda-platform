package hmda

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

import scala.concurrent.duration._
import scala.util.Random

object Greeter {
  def props(message: String, printerActor: ActorRef): Props =
    Props(new Greeter(message, printerActor))

  final case class WhoToGreet(who: String)
  case object Greet
}

class Greeter(message: String, printerActor: ActorRef) extends Actor {
  import Greeter._
  import Printer._

  var greeting = ""

  def receive: Receive = {
    case WhoToGreet(who) =>
      greeting = message + ", " + who
    case Greet =>
      //#greeter-send-message
      printerActor ! Greeting(greeting)
    //#greeter-send-message
  }
}

object Printer {
  def props: Props = Props[Printer]

  final case class Greeting(greeting: String)
}

class Printer extends Actor with ActorLogging {
  import Printer._

  def receive: Receive = {
    case Greeting(greeting) =>
      log.info("Greeting received (from " + sender() + "): " + greeting)
  }
}

object AkkaQuickstart extends App {
  import Greeter._

  // Create the 'helloAkka' actor system
  val system: ActorSystem = ActorSystem("helloAkka")

  //#create-actors
  // Create the printer actor
  val printer: ActorRef = system.actorOf(Printer.props, "printerActor")

  // Create the 'greeter' actors
  val howdyGreeter: ActorRef =
    system.actorOf(Greeter.props("Howdy", printer), "howdyGreeter")
  val helloGreeter: ActorRef =
    system.actorOf(Greeter.props("Hello", printer), "helloGreeter")
  val goodDayGreeter: ActorRef =
    system.actorOf(Greeter.props("Good day", printer), "goodDayGreeter")

  // Send messages
  howdyGreeter ! WhoToGreet("Akka")
  howdyGreeter ! Greet

  howdyGreeter ! WhoToGreet("Lightbend")
  howdyGreeter ! Greet

  helloGreeter ! WhoToGreet("Scala")
  helloGreeter ! Greet

  goodDayGreeter ! WhoToGreet("Play")
  goodDayGreeter ! Greet

  // Create an Akka Stream
  implicit val mat: ActorMaterializer = ActorMaterializer()(system)

  val exampleStream = Source
    .fromIterator(() => Iterator.range(0, 1000000))
    .map { x =>
      // random sleep (used for illustrative purposes only, do not block in production)
      Thread.sleep(
        FiniteDuration(math.abs(Random.nextLong()) % 1000,
                       TimeUnit.MILLISECONDS).toMillis
      )
      x
    }
    .to(Sink.foreach(println))
    .named("named-example")
    .run()
}
