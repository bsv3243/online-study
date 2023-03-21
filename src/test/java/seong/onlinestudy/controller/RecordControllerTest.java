package seong.onlinestudy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import seong.onlinestudy.dto.StudyRecordDto;
import seong.onlinestudy.service.RecordService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecordController.class)
class RecordControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    RecordService recordService;

    @Test
    void getRecords() throws Exception {
        //given

        given(recordService.getRecords(any(), any())).willReturn(List.of(new StudyRecordDto()));

        //when
        ResultActions resultActions = mvc.perform(get("/api/v1/records"));

        //then
        resultActions.andExpect(status().isOk())
                .andDo(print());
    }
}