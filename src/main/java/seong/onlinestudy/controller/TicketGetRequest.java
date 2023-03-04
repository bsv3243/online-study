package seong.onlinestudy.controller;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TicketGetRequest {

    @NotNull
    Long groupId;

    @NotNull
    LocalDate date;

    @NotNull
    int days;
}
