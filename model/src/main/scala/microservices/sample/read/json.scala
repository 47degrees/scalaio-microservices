package microservices.sample.read

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

import microservices.sample.read.Models.ConferenceAttendedResponse

object json extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val orderFormat = jsonFormat1(ConferenceAttendedResponse) // contains List[Item]
}
