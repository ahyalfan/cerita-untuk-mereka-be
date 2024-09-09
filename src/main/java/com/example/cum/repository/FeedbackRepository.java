package com.example.cum.repository;

import com.example.cum.entity.Donor;
import com.example.cum.entity.Feedback;
import com.example.cum.entity.Orphanages;
import com.example.cum.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, String> {
    Page<Feedback> findAllByDonor(Donor donor,
                                 Pageable pageable);
    Page<Feedback> findAllByOrphanages(Orphanages orphanages,
                                       Pageable pageable);
    Optional<Feedback> findFirstByDonorAndOrphanages(Donor donor, Orphanages orphanages);
}