package seong.onlinestudy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seong.onlinestudy.domain.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
