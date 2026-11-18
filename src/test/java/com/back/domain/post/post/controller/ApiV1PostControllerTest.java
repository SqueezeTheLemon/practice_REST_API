package com.back.domain.post.post.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // 테스트가 끝나면 롤백돼서 1번 글이 다시 살아나요
public class ApiV1PostControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;

    @Test
    @DisplayName("글 삭제")
    void t1() throws Exception {
        // Given: BaseInitData가 만든 1번 글 (댓글 3개가 딸려 있어요 → cascade REMOVE 확인용)
        int id = 1;

        // When
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/%d".formatted(id))
                )
                .andDo(print());

        // Then 1: 응답 검증
        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글이 삭제되었습니다.".formatted(id)));

        // Then 2: DB에서 정말 사라졌는지 검증
        assertThat(postService.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("글 단건조회")
    void t2() throws Exception {
        int id = 1;
        Post post = postService.findById(id).get(); // 기댓값은 db에서 꺼낸 진짜 데이터로 검증 준비

        // 이 경우를 테스트한다
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/%d".formatted(id))
                )
                .andDo(print());

        // 응답 검증
        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.createdDate").value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 16))))
                .andExpect(jsonPath("$.modifiedDate").value(Matchers.startsWith(post.getModifyDate().toString().substring(0, 16))))
                .andExpect(jsonPath("$.subject").value(post.getTitle()))
                .andExpect(jsonPath("$.body").value(post.getContent()));

    }

    @Test
    @DisplayName("글 다건조회")
    void t3() throws Exception {
        List<Post> posts = postService.findAll();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(posts.size()));

        for (int i = 0 ; i < posts.size() ; i++) {
            Post post = posts.get(i);

            resultActions.andExpect(jsonPath("$[%d].id".formatted(i)).value(post.getId()))
                    .andExpect(jsonPath("$[%d].createdDate".formatted(i)).value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 16))))
                    .andExpect(jsonPath("$[%d].modifiedDate".formatted(i)).value(Matchers.startsWith(post.getModifyDate().toString().substring(0, 16))))
                    .andExpect(jsonPath("$[%d].subject".formatted(i)).value(post.getTitle()))
                    .andExpect(jsonPath("$[%d].body".formatted(i)).value(post.getContent()));
        }

    }

    @Test
    @DisplayName("글 작성")
    void t4() throws Exception {
        // When: JSON 본문을 담아서 POST 요청
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON) // 본문이 JSON이라고 알려 줘요
                                .content("""
                                        {
                                            "title": "제목 new",
                                            "content": "내용 new"
                                        }
                                        """)
                )
                .andDo(print());

        // Given(사후): 방금 만들어진 글 = 가장 마지막 글
        Post post = postService.findAll().getLast();

        // Then
        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isCreated()) // resultCode 201-1 → 실제 HTTP 상태도 201이어야 해요
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글이 작성되었습니다.".formatted(post.getId())))
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.createdDate").value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 16))))
                .andExpect(jsonPath("$.data.modifiedDate").value(Matchers.startsWith(post.getModifyDate().toString().substring(0, 16))))
                .andExpect(jsonPath("$.data.subject").value("제목 new"))
                .andExpect(jsonPath("$.data.body").value("내용 new"));
    }

    @Test
    @DisplayName("글 수정")
    void t5() throws Exception {
        int id = 1;

        // When
        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/%d".formatted(id))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "제목 modified",
                                            "content": "내용 modified"
                                        }
                                        """)
                )
                .andDo(print());

        // Then 1: 응답 검증
        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글이 수정되었습니다.".formatted(id)));

        // Then 2: DB에 실제로 반영됐는지 검증
        Post post = postService.findById(id).get();
        assertThat(post.getTitle()).isEqualTo("제목 modified");
        assertThat(post.getContent()).isEqualTo("내용 modified");
    }
}
