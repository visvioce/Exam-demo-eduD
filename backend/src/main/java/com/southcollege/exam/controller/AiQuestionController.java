package com.southcollege.exam.controller;

import com.southcollege.exam.annotation.RequireRole;
import com.southcollege.exam.dto.request.GenerateQuestionRequest;
import com.southcollege.exam.dto.response.GeneratedQuestionResponse;
import com.southcollege.exam.dto.response.Result;
import com.southcollege.exam.enums.RoleEnum;
import com.southcollege.exam.service.AiQuestionService;
import com.southcollege.exam.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "AI 出题", description = "使用 AI 生成题目")
@RestController
@RequestMapping("/api/ai")
public class AiQuestionController {

    @Autowired
    private AiQuestionService aiQuestionService;

    @PostMapping("/generate-questions")
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    public Result<GeneratedQuestionResponse> generateQuestions(@Valid @RequestBody GenerateQuestionRequest request,
                                                                HttpServletRequest httpRequest) {
        Long userId = SecurityUtil.getCurrentUserId(httpRequest);
        GeneratedQuestionResponse response = aiQuestionService.generateQuestions(
                userId,
                request.getSubject(),
                request.getType(),
                request.getDifficulty(),
                request.getCount(),
                request.getRequirements()
        );
        return Result.success(response);
    }

    @PostMapping(value = "/generate-questions-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    @Operation(summary = "流式生成题目", description = "使用SSE实时推送AI生成的内容")
    public SseEmitter generateQuestionsStream(@Valid @RequestBody GenerateQuestionRequest request,
                                               HttpServletRequest httpRequest) {
        Long userId = SecurityUtil.getCurrentUserId(httpRequest);
        return aiQuestionService.generateQuestionsStream(
                userId,
                request.getSubject(),
                request.getType(),
                request.getDifficulty(),
                request.getCount(),
                request.getRequirements()
        );
    }
}
