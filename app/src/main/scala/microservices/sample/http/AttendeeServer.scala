package microservices.sample.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{complete, onSuccess, pathPrefix}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer

import microservices.sample.kafka.config.ScalaIOConfig
import microservices.sample.read.Models.ConferenceAttendedResponse
import microservices.sample.read.json

object AttendeeServer {

  import json._

  def route(service: AttendeeService)(implicit as: ActorSystem, m: Materializer) =
    pathPrefix("attendee" / Segment) { attendeeId =>
      onSuccess(service.getAttendedConferences(attendeeId)) {
        case r: ConferenceAttendedResponse
          if r.conferenceList.nonEmpty        => complete(r)
        case _: ConferenceAttendedResponse    => complete(StatusCodes.NotFound)
        case _                                => complete(StatusCodes.InternalServerError)
      }
    }

  def server(service: AttendeeService, config: ScalaIOConfig)(implicit as: ActorSystem, m: Materializer) =
    Http().bindAndHandle(route(service), config.server.host, config.server.port)
}
