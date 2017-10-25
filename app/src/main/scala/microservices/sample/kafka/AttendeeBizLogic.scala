package microservices.sample.kafka

import InternalModels.AttendeeState

class AttendeeBizLogic {
  def attendConference(l: AttendeeState, conferenceName: String): AttendeeState =
    l.copy(attendedConferences = l.attendedConferences + 1)
}

