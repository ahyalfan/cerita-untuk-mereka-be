package com.example.cum.service.impl;

import com.example.cum.dto.request.CreateStockRequest;
import com.example.cum.dto.request.DonationCreateRequest;
import com.example.cum.dto.request.UpdateStockRequest;
import com.example.cum.dto.response.DonationResponse;
import com.example.cum.dto.response.DonorResponse;
import com.example.cum.dto.response.PagebleResponse;
import com.example.cum.entity.*;
import com.example.cum.repository.*;
import com.example.cum.service.DonationService;
import com.example.cum.service.StockService;
import com.example.cum.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {
    private final DonationRepository donationRepository;
    private final BookRepository bookRepository;
    private final ValidationService validationService;
    private final OrphanagesRepository orphanagesRepository;
    private final StockService stockService;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final OrphanagesNeedRepository orphanagesNeedRepository;

    @Override
    @Transactional
    public void createDonation(User user, DonationCreateRequest request) {
        validationService.validate(request);
        var orphanages = orphanagesRepository.findById(request.getOrphanagesId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "orphanages not found"));
        Optional<Book> book = bookRepository.findByName(request.getBookName());

        Donation donation;
        if (book.isPresent()) {
            donation = Donation.builder()
                    .user(user)
                    .bookName(book.get().getName())
                    .orphanages(orphanages)
                    .donationStatus("pending")
                    .quantityDonated(request.getQuantityDonated())
                    .createdAt(System.currentTimeMillis())
                    .build();
        }else {
            donation = Donation.builder()
                    .user(user)
                    .bookName(request.getBookName())
                    .orphanages(orphanages)
                    .donationStatus("pending")
                    .quantityDonated(request.getQuantityDonated())
                    .createdAt(System.currentTimeMillis())
                    .build();
        }
        donationRepository.save(donation);
    }

    @Override
    public void deleteDonation(String id) {

    }

    @Override
    @Transactional
    public void updateStatus(String id, String statusold) {
        Donation donation = donationRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No found donation"));
        var status = statusold.toLowerCase();
        System.out.println(status);
//        if (!status.equals("pending") &&!status.equals("delivered") &&!status.equals("rejected")) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
//        }
        donation.setDonationStatus(status);
        donation.setUpdatedAt(System.currentTimeMillis());
        donationRepository.save(donation);
        if (status.equals("delivered")) {
            var orphanages = donation.getOrphanages();
            var booklowwer = donation.getBookName().toLowerCase();
            if (orphanages.getOrphanagesNeeds().size() > 0) {
                var need = orphanages.getOrphanagesNeeds().stream().filter(n -> n.getBookName().equals(booklowwer)).filter(n -> n.getStatus().equals("requested")).findFirst();
                System.out.println(need);
                if (need.isPresent()) {
                    need.get().setTargetQuantity(donation.getQuantityDonated());
                    if (need.get().getTargetQuantity() >= need.get().getQuantity()){
                        need.get().setStatus("completed");
                        need.get().setUpdatedAt(System.currentTimeMillis());
                    }
                }
            }
            Optional<Stock> stock= stockRepository.findByBookNameAndOrphanages(donation.getBookName(), donation.getOrphanages());
            if (stock.isPresent()) {
                var req = UpdateStockRequest.builder()
                        .bookName(donation.getBookName())
                       .orphanageId(donation.getOrphanages().getId())
                        .quantity(donation.getQuantityDonated())
                        .build();
                stockService.updateStock(req);
            }else {
                var req = CreateStockRequest.builder()
                        .bookName(donation.getBookName())
                       .orphanageId(donation.getOrphanages().getId())
                        .quantity(donation.getQuantityDonated())
                        .build();
                stockService.createStock(req);
            }
        }
    }

    @Override
    public DonationResponse getDonationById(String id) {
        Donation donation = donationRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No found donation"));
        return convert(donation);
    }

    @Override
    public PagebleResponse<DonationResponse> getDonationsByUser(String userId, int page, int limit) {
        Pageable pageable = PageRequest.of(page,limit);
        User user = userRepository.findById(userId).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        var donations = donationRepository.findAllByUser(user, pageable);
        return PagebleResponse.<DonationResponse>builder()
                .total_page(donations.getTotalPages())
                .page(page)
                .limit(limit)
                .data(donations.stream().map(this::convert).toList())
                .build();

    }

    @Override
    public PagebleResponse<DonationResponse> getDonationsByOrphanage(String orphanageId, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Orphanages orphanages = orphanagesRepository.findById(orphanageId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orphanage not found"));
        var donations = donationRepository.findAllByOrphanages(orphanages, pageable);
        return PagebleResponse.<DonationResponse>builder()
               .total_page(donations.getTotalPages())
               .page(page)
               .limit(limit)
               .data(donations.stream().map(this::convert).toList())
               .build();
    }

    @Override
    public PagebleResponse<DonationResponse> getAllDonations(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Order.desc("createdAt")));
        var donations = donationRepository.findAll(pageable);
        return PagebleResponse.<DonationResponse>builder()
               .total_page(donations.getTotalPages())
               .page(page)
               .limit(limit)
               .data(donations.stream().map(this::convert).toList())
               .build();
    }

    public PagebleResponse<Map.Entry<String,Integer>> getSumDonationsByGroupDonor(int page,int limit) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Order.desc("createdAt")));
        var donations = donationRepository.findAllByDonationStatus("delivered",pageable);
        Map<String, Integer> donorSum = new HashMap<>();
        for (Donation d : donations) {
            if (donorSum.containsKey(d.getUser().getUsername())) {
                donorSum.put(d.getUser().getUsername(), donorSum.get(d.getUser().getUsername()) + d.getQuantityDonated());
            } else {
                donorSum.put(d.getUser().getUsername(), d.getQuantityDonated());
            }
        }
        return PagebleResponse.<Map.Entry<String, Integer>>builder()
               .total_page(1)
               .page(page)
               .limit(limit)
               .data(donorSum.entrySet())
               .build();

    }

    @Override
    public PagebleResponse<DonationResponse> getDonationsByStatus(String statusOld, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        var status = statusOld.toLowerCase();
        if (!status.equals("pending") &&!status.equals("delivered") &&!status.equals("rejected")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
        }
        var donations = donationRepository.findAllByDonationStatus(status, pageable);
        return PagebleResponse.<DonationResponse>builder()
               .total_page(donations.getTotalPages())
               .page(page)
               .limit(limit)
               .data(donations.stream().map(this::convert).toList())
               .build();
    }

    private DonationResponse convert(Donation donation) {
        return DonationResponse.builder()
               .id(donation.getId())
               .bookName(donation.getBookName())
               .status(donation.getDonationStatus())
               .quantityDonated(donation.getQuantityDonated())
               .orphanagesId(donation.getOrphanages().getId())
               .userId(donation.getUser().getId())
                .createdAt(donation.getCreatedAt())
                .updatedAt(donation.getUpdatedAt())
               .build();
    }
}
