package seong.onlinestudy.controller;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GroupsDeleteRequest {

    @NotNull(message = "회원 아이디는 필수입니다.")
    private Long MemberId;
}
