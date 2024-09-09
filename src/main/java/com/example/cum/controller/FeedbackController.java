package com.example.cum.controller;

import com.example.cum.dto.request.CreateFeedbackRequest;
import com.example.cum.dto.request.UpdateFeedbackRequest;
import com.example.cum.dto.response.FeedbackResponse;
import com.example.cum.dto.response.PagebleResponse;
import com.example.cum.dto.response.WebResponse;
import com.example.cum.service.FeedbackService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/feedback")
@SecurityRequirement(name = "Authorization")
public class FeedbackController {
    private final FeedbackService feedbackService;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> create(@RequestBody CreateFeedbackRequest request){
        feedbackService.createFeedback(request);
        return WebResponse.<String>builder()
                .status(HttpStatus.CREATED)
                .message("Feedback created successfully")
                .data("")
               .build();
    }

    @DeleteMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> delete(@PathVariable String id){
        log.info("Deleting feedback for id {}", id);
        feedbackService.deleteFeedback(id);
        return WebResponse.<String>builder()
               .status(HttpStatus.OK)
               .message("Feedback deleted successfully")
               .build();
    }

    @PutMapping(
            path = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> update(@PathVariable String id, @RequestBody UpdateFeedbackRequest request){
        log.info("Updating feedback for id {}", id);
        request.setId(id);
        feedbackService.updateFeedback(request);
        return WebResponse.<String>builder()
               .status(HttpStatus.OK)
               .message("Feedback updated successfully")
               .data("")
               .build();
    }

    @GetMapping(
            path = "/by-orphanages/{orphanagesId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<PagebleResponse<FeedbackResponse>> getFeedback(@PathVariable String orphanagesId, @RequestParam int page){
        log.info("Getting feedback for id {}", orphanagesId);
        System.out.println(page);
        var feedback = feedbackService.getFeedbackByOrphanagesId(orphanagesId,page,15);
        return WebResponse.<PagebleResponse<FeedbackResponse>>builder()
               .status(HttpStatus.OK)
               .message("Get feedback successfully")
               .data(feedback)
               .build();
    }
    @GetMapping(
            path = "/by-donor/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<PagebleResponse<FeedbackResponse>> getFeedbackByUser(@PathVariable String id, @RequestParam(defaultValue = "0") int page){
        log.info("Getting feedback for id {}", id);
        var feedback = feedbackService.getFeedbackByDonor(id,page,15);
        return WebResponse.<PagebleResponse<FeedbackResponse>>builder()
               .status(HttpStatus.OK)
               .message("Get feedback successfully")
               .data(feedback)
               .build();
    }

    @GetMapping(
            path = "/donor/{donorId}/orphanages/{orphanagesId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<Boolean> getFeedbackByDonorAndOrphanages(@PathVariable String donorId, @PathVariable String orphanagesId) {
        log.info("Getting feedback for id {}", donorId);
        var feedback = feedbackService.checkFeedForDonorByOrphanages(donorId, orphanagesId);
        return WebResponse.<Boolean>builder()
                .status(HttpStatus.OK)
                .message("Get feedback successfully")
                .data(feedback)
                .build();
    }
}
