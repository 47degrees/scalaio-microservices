package microservices.sample.read

object Models {
  final case class ConferenceAttendedResponse(
    conferenceList: List[String] = List.empty[String])
}