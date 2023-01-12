package seong.onlinestudy.domain;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    private int number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    public static Room createRoom(int number) {
        Room room = new Room();
        room.number = number;

        return room;
    }
}
