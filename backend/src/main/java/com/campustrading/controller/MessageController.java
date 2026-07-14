package com.campustrading.controller;

import com.campustrading.common.Result;
import com.campustrading.dto.ConversationResponse;
import com.campustrading.dto.MessageResponse;
import com.campustrading.dto.SendMessageRequest;
import com.campustrading.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public Result<ConversationResponse> sendMessage(Authentication authentication,
                                                     @Valid @RequestBody SendMessageRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(messageService.sendMessage(userId, request));
    }

    @GetMapping("/conversations")
    public Result<Page<ConversationResponse>> getConversations(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(messageService.getConversationList(userId, page, size));
    }

    @GetMapping("/conversations/{id}")
    public Result<Page<MessageResponse>> getMessages(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(messageService.getMessages(userId, id, page, size));
    }

    @PutMapping("/conversations/{id}/read")
    public Result<Void> markRead(Authentication authentication, @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        messageService.markConversationRead(userId, id);
        return Result.success();
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        long count = messageService.getTotalUnreadCount(userId);
        return Result.success(Map.of("count", count));
    }
}
