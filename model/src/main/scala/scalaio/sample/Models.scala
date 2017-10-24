package scalaio.sample

object Models {

  abstract class Payload extends Product with Serializable

  sealed trait Command extends Payload
  final case class AttendConferenceCommand(attendeeId: String, conferenceName: String) extends Command

  sealed trait Event extends Payload
  case class ConferenceAttendedEvent(attendee: String, conference: String) extends Event

  case class EntityEnvelope(id: String, payload: Payload)

  final case class AttendeeState(lists: List[String])

}