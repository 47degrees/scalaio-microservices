package microservices.sample.http

import akka.NotUsed
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.Materializer
import akka.stream.scaladsl.Source

import scala.concurrent.Future
import microservices.sample.kafka.implicits.system
import microservices.sample.read.Models.ConferenceAttendedResponse
import microservices.sample.write.Models.ConferenceAttendedEvent

    class AttendeeService() {
      def getAttendedConferences(attendeeId: String)(
        implicit m: Materializer): Future[ConferenceAttendedResponse] = {
        val queries = PersistenceQuery(system)
          .readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)
        val events: Source[EventEnvelope, NotUsed] = queries
          .currentEventsByPersistenceId(attendeeId, 0, Long.MaxValue)

        events.runFold(ConferenceAttendedResponse()) { (res, journal) =>
          journal.event match {
            case event: ConferenceAttendedEvent =>
              ConferenceAttendedResponse(
                res.conferenceList :+ event.conference)
            case _ => ConferenceAttendedResponse(res.conferenceList)
          }

        }
      }
    }
