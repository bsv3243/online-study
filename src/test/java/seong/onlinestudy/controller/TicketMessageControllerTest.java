package seong.onlinestudy.controller;

import lombok.Getter;
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
import seong.onlinestudy.repository.GroupRepository;
import seong.onlinestudy.repository.MemberRepository;
import seong.onlinestudy.repository.StudyRepository;
import seong.onlinestudy.repository.TicketRepository;
import seong.onlinestudy.request.GroupCreateRequest;
import seong.onlinestudy.request.TicketCreateRequest;
import seong.onlinestudy.websocket.TicketMessage;

import javax.persistence.EntityManager;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static seong.onlinestudy.MyUtils.*;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TicketMessageControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    EntityManager em;
    @Autowired
    TicketRepository ticketRepository;
    @Autowired
    StudyRepository studyRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    GroupRepository groupRepository;

    StompSession stompSession;
    CompletableFuture<TicketDto> completableFuture;

    private final WebSocketStompClient client;

    public TicketMessageControllerTest() {
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

    @AfterEach
    public void disconnect() {
        if(this.stompSession.isConnected()) {
            this.stompSession.disconnect();
        }
    }

    @Test
    @DisplayName("웹소켓 정상 연결 테스트")
    public void initTest() {

    }

    @Test
    @DisplayName("메시지 전송 테스트") //메시지 전송은 되나 Ticket 을 조회하지 못하고 있음
    void sendTicket() throws ExecutionException, InterruptedException, TimeoutException {
        //given
        Member member = createMember("member", "member");
        memberRepository.save(member);
        Group group = createGroup("group", 30, member);
        groupRepository.save(group);
        Study study = createStudy("study");
        studyRepository.save(study);
        Ticket ticket = createTicket(TicketStatus.STUDY, member, study, group);
        ticketRepository.save(ticket);
        em.flush();
        em.clear();

        CustomStompFrameHandler<TicketDto> handler = new CustomStompFrameHandler<>(TicketDto.class);
        this.stompSession.subscribe("/sub/group/" + group.getId(), handler);

        //when
        Ticket findTicket = ticketRepository.findById(ticket.getId()).get();
        assertThat(findTicket).isNotNull();
        this.stompSession.send("/pub/groups", new TicketMessage(ticket.getId(), group.getId()));

        //then
        TicketDto ticketDto = handler.getCompletableFuture().get(3, TimeUnit.SECONDS);

    }

    static class CustomStompFrameHandler<T> implements StompFrameHandler {

        @Getter
        private final CompletableFuture<T> completableFuture = new CompletableFuture<>();

        private final Class<T> tClass;

        public CustomStompFrameHandler(Class<T> tClass) {
            this.tClass = tClass;
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return this.tClass;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            completableFuture.complete((T) payload);
        }

    }


    private List<Transport> createTransport() {
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }
}