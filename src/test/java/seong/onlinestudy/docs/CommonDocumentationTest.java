package seong.onlinestudy.docs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.PayloadSubsectionExtractor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import seong.onlinestudy.enumtype.EnumType;
import seong.onlinestudy.enumtype.GroupCategory;
import seong.onlinestudy.enumtype.OrderBy;
import seong.onlinestudy.enumtype.PostCategory;

import java.util.Arrays;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(DocumentController.class)
@AutoConfigureRestDocs
public class CommonDocumentationTest {

    @Autowired
    MockMvc mvc;

    @Test
    public void commonResult() throws Exception {
        //given

        //when
        ResultActions resultActions = mvc.perform(get("/result")
                .accept(MediaType.APPLICATION_JSON));

        //then
        resultActions
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("common-result",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                attributes(key("title").value("공통 응답")),
                                fieldWithPath("code").type(JsonFieldType.STRING).description("HTTP 응답 코드"),
                                subsectionWithPath("data").type(JsonFieldType.VARIES).description("응답 데이터")
                        )));
    }

    @Test
    public void commonPageResult() throws Exception {
        //given

        //when
        ResultActions resultActions = mvc.perform(get("/page-result")
                .accept(MediaType.APPLICATION_JSON));

        //then
        resultActions
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("common-page-result",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                attributes(key("title").value("공통 응답(페이지 정보)")),
                                fieldWithPath("code").type(JsonFieldType.STRING).description("HTTP 응답 코드"),
                                subsectionWithPath("data").type(JsonFieldType.VARIES).description("응답 데이터"),
                                fieldWithPath("number").type(JsonFieldType.NUMBER).description("페이지 번호"),
                                fieldWithPath("size").type(JsonFieldType.NUMBER).description("페이지 사이즈"),
                                fieldWithPath("totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수"),
                                fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 여부"),
                                fieldWithPath("hasPrevious").type(JsonFieldType.BOOLEAN).description("이전 페이지 여부")
                        )));
    }

    @Test
    public void commonPageCategory() throws Exception {
        //given

        //when
        ResultActions resultActions = mvc.perform(get("/post-category")
                .accept(MediaType.APPLICATION_JSON));

        //then
        resultActions
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("post-category",
                                customResponseFields("custom-response", attributes(key("title").value("게시글 카테고리")),
                                        beneathPath("data").withSubsectionId("post-category"),
                                        enumConvertFieldDescriptor(PostCategory.values()))));
    }

    @Test
    public void commonGroupCategory() throws Exception {
        //given

        //when
        ResultActions resultActions = mvc.perform(get("/group-category")
                .accept(MediaType.APPLICATION_JSON));

        //then
        resultActions
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("group-category",
                        customResponseFields("custom-response", attributes(key("title").value("그룹 카테고리")),
                                beneathPath("data").withSubsectionId("group-category"),
                                enumConvertFieldDescriptor(GroupCategory.values()))));
    }

    @Test
    public void commonOrderBy() throws Exception {
        //given

        //when
        ResultActions resultActions = mvc.perform(get("/order-by")
                .accept(MediaType.APPLICATION_JSON));

        //then
        resultActions
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("order-by",
                        customResponseFields("custom-response", attributes(key("title").value("그룹 정렬 순서")),
                                beneathPath("data").withSubsectionId("order-by"),
                                enumConvertFieldDescriptor(OrderBy.values()))));
    }

    private FieldDescriptor[] enumConvertFieldDescriptor(EnumType[] enumTypes) {
        return Arrays.stream(enumTypes)
                .map(enumType -> fieldWithPath(enumType.getName()).description(enumType.getText()))
                .toArray(FieldDescriptor[]::new);
    }

    public static CustomResponseFieldsSnippet customResponseFields(String type,
                                                                   Map<String, Object> attributes,
                                                                   PayloadSubsectionExtractor<?> subsectionExtractor,
                                                                   FieldDescriptor... descriptors) {
        return new CustomResponseFieldsSnippet(type, Arrays.asList(descriptors), attributes,
                true, subsectionExtractor);
    }
}
