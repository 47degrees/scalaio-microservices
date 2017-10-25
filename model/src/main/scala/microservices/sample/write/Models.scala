package microservices.sample.write

object Models {
  sealed abstract class Payload extends Product with Serializable
  sealed trait Command extends Payload
  sealed trait Event extends Payload

  final case class AttendConferenceCommand(
    attendeeId: String,
    conferenceName: String) extends Command

  final case class ConferenceAttendedEvent(
    attendee: String,
    conference: String) extends Event
}