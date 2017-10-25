package microservices.sample.kafka

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContextExecutor
import microservices.sample.kafka.config.ScalaIOConfig

object implicits {
  implicit val scalaioConfig: ScalaIOConfig = ScalaIOConfig(ConfigFactory.load())
  implicit val system: ActorSystem = ActorSystem("scalaio-microservices")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = scalaioConfig.timeout
  implicit val materialiser: Materializer = ActorMaterializer()
}
