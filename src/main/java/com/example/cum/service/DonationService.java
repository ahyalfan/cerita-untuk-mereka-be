package com.example.cum.service;

import com.example.cum.dto.request.DonationCreateRequest;
import com.example.cum.dto.response.DonationResponse;
import com.example.cum.dto.response.DonorResponse;
import com.example.cum.dto.response.PagebleResponse;
import com.example.cum.entity.User;

import java.util.Map;

public interface DonationService {
    void createDonation(User user, DonationCreateRequest request);
    void deleteDonation(String id);
    void updateStatus(String id, String status);
    DonationResponse getDonationById(String id);
    PagebleResponse<DonationResponse> getDonationsByUser(String userId,int page, int limit);
    PagebleResponse<DonationResponse> getDonationsByOrphanage(String orphanageId,int page, int limit);
    PagebleResponse<DonationResponse> getAllDonations(int page, int limit);
    PagebleResponse<DonationResponse> getDonationsByStatus(String status, int page, int limit);
    PagebleResponse<Map.Entry<String,Integer>> getSumDonationsByGroupDonor(int page, int limit);
}
