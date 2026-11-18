package com.back.domain.post.post.controller;

import com.back.domain.post.post.dto.PostDto;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController // @Controller + @ResponseBody
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {
    private final PostService postService;

    @GetMapping
    @Transactional(readOnly = true)
    public List<PostDto> getItems() {
        List<Post> items = postService.findAll();

        return items
                .stream()
                .map(p -> new PostDto(p))
                .toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PostDto getItem(@PathVariable int id){
        Post post = postService.findById(id).get();
        return new PostDto(post);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public RsData<Void> delete(@PathVariable int id){
        Post post = postService.findById(id).get();
        postService.delete(post);
        return new RsData<Void>("200-1", "%d번 글이 삭제되었습니다.".formatted(id));
    }

    // 요청 본문(JSON)을 받는 전용 record. id, 날짜는 클라이언트가 정하면 안 되니까 없어요
    public record PostWriteReqBody(
            @NotBlank
            @Size(min = 2, max = 100)
            String title,

            @NotBlank
            @Size(min = 2, max = 5000)
            String content
    ) {
    }

    @PostMapping
    @Transactional
    public RsData<PostDto> write(
            @RequestBody @Valid PostWriteReqBody reqBody // @Valid가 있어야 위의 검증 어노테이션이 작동해요
    ) {
        Post post = postService.write(reqBody.title(), reqBody.content());

        return new RsData<>(
                "201-1",
                "%d번 글이 작성되었습니다.".formatted(post.getId()),
                new PostDto(post)
        );
    }

    public record PostModifyReqBody(
            @NotBlank
            @Size(min = 2, max = 100)
            String title,

            @NotBlank
            @Size(min = 2, max = 5000)
            String content
    ) {
    }

    @PutMapping("/{id}")
    @Transactional
    public RsData<Void> modify(
            @PathVariable int id,
            @RequestBody @Valid PostModifyReqBody reqBody
    ) {
        Post post = postService.findById(id).get();
        postService.modify(post, reqBody.title(), reqBody.content()); // 💡 save 없이도 트랜잭션 끝에 변경 감지(dirty checking)로 UPDATE돼요

        return new RsData<>(
                "200-1",
                "%d번 글이 수정되었습니다.".formatted(id)
        );
    }
}