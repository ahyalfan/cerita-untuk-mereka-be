package com.example.cum.service;

import com.example.cum.dto.request.CreateFeedbackRequest;
import com.example.cum.dto.request.UpdateFeedbackRequest;
import com.example.cum.dto.response.FeedbackResponse;
import com.example.cum.dto.response.PagebleResponse;

public interface FeedbackService {
    void createFeedback(CreateFeedbackRequest request);
    void deleteFeedback(String id);
    void updateFeedback(UpdateFeedbackRequest request);
    PagebleResponse<FeedbackResponse> getFeedbackAll(int page ,int limit);
    PagebleResponse<FeedbackResponse> getFeedbackByDonor(String donorId,int page ,int limit);
    PagebleResponse<FeedbackResponse> getFeedbackByOrphanagesId(String orphanagesId,int page ,int limit);
    boolean checkFeedForDonorByOrphanages(String donorId, String orphanageId);
}
