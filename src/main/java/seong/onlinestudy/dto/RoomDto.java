package seong.onlinestudy.dto;

import lombok.Data;
import seong.onlinestudy.domain.Room;

@Data
public class RoomDto {

    private Long roomId;
    private int number;

    public static RoomDto from(Room room) {
        RoomDto roomDto = new RoomDto();
        roomDto.roomId = room.getId();
        roomDto.number = room.getNumber();

        return roomDto;
    }
}
