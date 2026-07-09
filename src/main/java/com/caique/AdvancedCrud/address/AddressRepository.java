package com.caique.AdvancedCrud.address;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    Page<Address> findByUserId(Long userId, Pageable pageable);

    boolean existsByUserId(Long userId);

    Optional<Address> findByPublicIdAndUserId(UUID publicId, Long userId);

    Optional<Address> findByUserIdAndPrimaryIsTrue(Long userId);

    Optional<Address> findFirstByUserIdOrderByCreatedAtAsc(Long userId);

}
