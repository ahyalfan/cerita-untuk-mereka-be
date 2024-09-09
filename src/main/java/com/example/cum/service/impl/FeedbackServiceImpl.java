package com.example.cum.service.impl;

import com.example.cum.dto.request.CreateFeedbackRequest;
import com.example.cum.dto.request.UpdateFeedbackRequest;
import com.example.cum.dto.response.FeedbackResponse;
import com.example.cum.dto.response.PagebleResponse;
import com.example.cum.entity.Feedback;
import com.example.cum.repository.DonorRepository;
import com.example.cum.repository.FeedbackRepository;
import com.example.cum.repository.OrphanagesRepository;
import com.example.cum.repository.UserRepository;
import com.example.cum.service.FeedbackService;
import com.example.cum.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {
    private final DonorRepository donorRepository;
    private final OrphanagesRepository orphanagesRepository;
    private final FeedbackRepository feedbackRepository;
    private final ValidationService validationService;

    @Override
    @Transactional
    public void createFeedback(CreateFeedbackRequest request) {
        validationService.validate(request);

        var donor = donorRepository.findById(request.getDonorId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "donor not found"));
        var orphanages = orphanagesRepository.findById(request.getOrphanagesId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orphanages not found"));
        Feedback feedback = Feedback.builder()
                .donor(donor)
                .orphanages(orphanages)
                .feedbackText(request.getFeedbackText())
                .createdAt(System.currentTimeMillis())
                .build();
        feedbackRepository.save(feedback);
    }

    @Override
    @Transactional
    public void deleteFeedback(String id) {
        var feedback = feedbackRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback not found"));
        feedbackRepository.delete(feedback);
    }

    @Override
    public void updateFeedback(UpdateFeedbackRequest request) {
        validationService.validate(request);
        var donor = donorRepository.findById(request.getDonorId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found"));
        var orphanages = orphanagesRepository.findById(request.getOrphanagesId()).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orphanages not found"));
        var feedback = feedbackRepository.findById(request.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback not found"));
        feedback.setFeedbackText(request.getFeedbackText());
        feedback.setUpdatedAt(System.currentTimeMillis());
        feedbackRepository.save(feedback);
    }

    @Override
    public PagebleResponse<FeedbackResponse> getFeedbackAll(int page ,int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        var feedback = feedbackRepository.findAll(pageable);
        return PagebleResponse.<FeedbackResponse>builder()
                .total_page(feedback.getTotalPages())
                .page(page)
                .limit(limit)
                .data(feedback.stream().map(this::convert).toList())
               .build();
    }

    @Override
    public PagebleResponse<FeedbackResponse> getFeedbackByDonor(String donorId,int page ,int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        var donor = donorRepository.findById(donorId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "donor not found"));
        var feedback = feedbackRepository.findAllByDonor(donor,pageable);
        return PagebleResponse.<FeedbackResponse>builder()
                .total_page(feedback.getTotalPages())
                .page(page)
                .limit(limit)
                .data(feedback.stream().map(this::convert).toList())
               .build();
    }

    @Override
    public PagebleResponse<FeedbackResponse> getFeedbackByOrphanagesId(String orphanagesId,int page ,int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        System.out.println("test page ");
        System.out.println(orphanagesId);
        System.out.println("test page ");
        var orphanages = orphanagesRepository.findById(orphanagesId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orphanages not found in feedback"));
        var feedback = feedbackRepository.findAllByOrphanages(orphanages,pageable);
        return PagebleResponse.<FeedbackResponse>builder()
               .total_page(feedback.getTotalPages())
               .page(page)
               .limit(limit)
               .data(feedback.stream().map(this::convert).toList())
               .build();
    }

    public boolean checkFeedForDonorByOrphanages(String donorId, String orphanageId) {
        var donor = donorRepository.findById(donorId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "donor not found"));
        var orphanages = orphanagesRepository.findById(orphanageId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orphanages not found"));

        return feedbackRepository.findFirstByDonorAndOrphanages(donor, orphanages).isPresent();
    }

    private FeedbackResponse convert(Feedback feedback) {
        return FeedbackResponse.builder()
               .id(feedback.getId())
               .donorId(feedback.getDonor().getId())
               .orphanagesId(feedback.getOrphanages().getId())
               .feedbackText(feedback.getFeedbackText())
               .createdAt(feedback.getCreatedAt())
               .updatedAt(feedback.getUpdatedAt())
               .build();
    }
}
