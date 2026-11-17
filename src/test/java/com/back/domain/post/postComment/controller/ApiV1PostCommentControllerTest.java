package com.back.domain.post.postComment.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc; // 💡 Boot 4 경로예요!
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest           // 스프링 전체를 띄워요
@AutoConfigureMockMvc     // MockMvc를 주입받을 수 있게 해 줘요
@ActiveProfiles("test")   // application-test.yaml (테스트용 H2)을 써요
@Transactional            // 테스트가 끝나면 롤백 → 다른 테스트에 영향 없어요
public class ApiV1PostCommentControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;

    @Test
    @DisplayName("댓글 삭제")
    void t1() throws Exception {
        // Given: BaseInitData가 1번 글에 댓글 1~3번을 만들어 둬요
        int postId = 1;
        int id = 1;

        // When: DELETE 요청 보내기
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/%d/comments/%d".formatted(postId, id))
                )
                .andDo(print()); // 🔍 요청/응답 전체를 콘솔에 찍어 줘요

        // Then 1: 응답 검증
        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class)) // 어느 컨트롤러가
                .andExpect(handler().methodName("delete"))                         // 어느 메서드가 처리했는지
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 댓글이 삭제되었습니다.".formatted(id)));

        // Then 2: 실제로 사라졌는지 검증
        Post post = postService.findById(postId).get();
        assertThat(post.findCommentById(id)).isEmpty(); // Optional이 비어 있으면 삭제 성공!
    }
}