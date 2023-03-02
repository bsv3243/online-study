package seong.onlinestudy.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import seong.onlinestudy.MyUtils;
import seong.onlinestudy.domain.*;
import seong.onlinestudy.dto.TicketDto;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.repository.StudyRepository;
import seong.onlinestudy.repository.TicketRepository;
import seong.onlinestudy.request.GroupCreateRequest;
import seong.onlinestudy.request.TicketCreateRequest;
import seong.onlinestudy.websocket.Message;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoomControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    TicketRepository ticketRepository;
    @Autowired
    StudyRepository studyRepository;
    @Autowired
    MemberRepository memberRepository;

    StompSession stompSession;
    CompletableFuture<TicketDto> completableFuture;

    private final WebSocketStompClient client;

    public RoomControllerTest() {
        this.client = new WebSocketStompClient(new SockJsClient(createTransport()));
        this.client.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @BeforeEach
    public void connect() throws ExecutionException, InterruptedException, TimeoutException {
        this.stompSession = this.client
                .connect("ws://localhost:" + port + "/ws", new StompSessionHandlerAdapter() {
                })
                .get(3, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("웹소켓 정상 연결 테스트")
    public void initTest() {

    }

    @Test
    @Transactional
    @DisplayName("메시지 전송 테스트")
    public void sendTicket() throws ExecutionException, InterruptedException, TimeoutException {
        Study study = createStudy();
        studyRepository.save(study);

        Member member = createMember();
        memberRepository.save(member);

        Group group = createGroup("테스트그룹", 30, member);

        Ticket ticket = MyUtils.createTicket(TicketStatus.STUDY, member, study, group);
        ticketRepository.save(ticket);

        Message message = new Message();
        message.setTicketId(ticket.getId());

        stompSession.subscribe("/topic/room/1", new CustomStompFrameHandler());
        stompSession.send("/app/room/1", message);
        TicketDto ticketDto = completableFuture.get(5, TimeUnit.SECONDS);

        Assertions.assertThat(ticketDto).isNotNull();
    }

    @AfterEach
    public void disconnect() {
        if(this.stompSession.isConnected()) {
            this.stompSession.disconnect();
        }
    }

    private class CustomStompFrameHandler implements StompFrameHandler {

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return TicketDto.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            completableFuture.complete((TicketDto) payload);
        }

    }


    private List<Transport> createTransport() {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }

    private TicketCreateRequest createTicketRequest(Long groupId, Long studyId) {
        TicketCreateRequest request = new TicketCreateRequest();
        request.setGroupId(groupId);
        request.setStudyId(studyId);

        return request;
    }

    private Group createGroup(String name, int headcount, Member member) {
        GroupCreateRequest request = new GroupCreateRequest();
        request.setName(name);
        request.setHeadcount(headcount);

        GroupMember groupMember = GroupMember.createGroupMember(member, GroupRole.MASTER);

        return Group.createGroup(request, groupMember);
    }

    private Study createStudy() {
        Study study = new Study();

        setField(study, "name", "테스트");

        return study;
    }

    private Member createMember() {
        Member member = new Member();

        setField(member, "username", "test1234");
        setField(member, "password", "test1234");

        return member;
    }
}