package com.example.cum.controller;

import com.example.cum.dto.request.DonationCreateRequest;
import com.example.cum.dto.response.DonationResponse;
import com.example.cum.dto.response.PagebleResponse;
import com.example.cum.dto.response.WebResponse;
import com.example.cum.entity.User;
import com.example.cum.service.DonationService;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/donations")
@SecurityRequirement(name = "Authorization")
public class DonationController {
    private final DonationService donationService;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> createDonation(@RequestBody DonationCreateRequest request, User user){
        log.info("Creating donation for user {}", user.getId());
        donationService.createDonation(user, request);
        return WebResponse.<String>builder()
               .status(HttpStatus.CREATED)
               .message("Donation created successfully")
                .data("")
               .build();
    }

    @PutMapping(
            path = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> updateDonationStatus(@PathVariable String id,
                                                    @RequestBody String status){
        String result = status.replace("\"", "");
        donationService.updateStatus(id, result);
        return WebResponse.<String>builder()
               .status(HttpStatus.OK)
               .message("Donation status updated successfully")
               .data("")
               .build();
    }

    @GetMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<DonationResponse> getDonation(@PathVariable String id){
        log.info("Getting donation for id {}", id);
        var donation = donationService.getDonationById(id);
        return WebResponse.<DonationResponse>builder()
               .status(HttpStatus.OK)
               .message("Donation retrieved successfully")
               .data(donation)
               .build();
    }

    @GetMapping(
            path = "/user/{userId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<PagebleResponse> getAllDonations(@PathVariable String userId, @RequestParam(defaultValue = "0") int page){
        log.info("Getting all donations for user {}", userId);
        var donations = donationService.getDonationsByUser(userId, page, 15);
        return WebResponse.<PagebleResponse>builder()
               .status(HttpStatus.OK)
               .message("Donations retrieved successfully")
               .data(donations)
               .build();
    }

    @GetMapping(
            path = "/orphanage/{orphanageId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<PagebleResponse> getAllDonationsByOrphanage(@PathVariable String orphanageId, @RequestParam(defaultValue = "0") int page){
        log.info("Getting all donations for orphanage {}", orphanageId);
        var donations = donationService.getDonationsByOrphanage(orphanageId, page, 15);
        return WebResponse.<PagebleResponse>builder()
               .status(HttpStatus.OK)
               .message("Donations retrieved successfully")
               .data(donations)
               .build();
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<PagebleResponse<DonationResponse>> getAllDonations(@RequestParam(defaultValue = "0") int page){
        log.info("Getting all donations");
        var donations = donationService.getAllDonations(page, 15);
        return WebResponse.<PagebleResponse<DonationResponse>>builder()
               .status(HttpStatus.OK)
               .message("Donations retrieved successfully")
               .data(donations)
               .build();
    }

    @GetMapping(
            path = "/groups-donor",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<PagebleResponse<Map.Entry<String,Integer>>> getSum(@RequestParam(defaultValue = "0") int page){
        log.info("Getting donations by status {}", page);
        var donations = donationService.getSumDonationsByGroupDonor(page, 15);
        return WebResponse.<PagebleResponse<Map.Entry<String,Integer>>>builder()
               .status(HttpStatus.OK)
               .message("Donations retrieved successfully")
               .data(donations)
               .build();
    }
}
