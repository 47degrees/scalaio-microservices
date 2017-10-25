package microservices.sample

import akka.actor.Props

import microservices.sample.http.{AttendeeServer, AttendeeService}
import microservices.sample.kafka._
import microservices.sample.kafka.implicits

object Boot extends App {

  import implicits._

  val kafkaProducer = ScalaIOProducer.create(scalaioConfig)
  val output = system.actorOf(Props(new PublisherActor(scalaioConfig, kafkaProducer)))
  val attendeeBehaviour = new AttendeeBizLogic()
  val attendeesRegion = AttendeeShardingRegion(attendeeBehaviour, output, scalaioConfig)

  ScalaIOConsumer.start(attendeesRegion, scalaioConfig)

  // WebServer
  val service = new AttendeeService()

  AttendeeServer.server(service, scalaioConfig)

}
