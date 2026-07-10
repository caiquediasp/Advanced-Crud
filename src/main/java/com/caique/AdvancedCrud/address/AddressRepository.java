package com.caique.AdvancedCrud.address;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    Page<Address> findByUser_PublicIdAndUser_DeletedAtIsNull(UUID userPublicId, Pageable pageable);

    Optional<Address> findByPublicIdAndUser_PublicIdAndUser_DeletedAtIsNull(UUID publicId, UUID userPublicId);

    Optional<Address> findByUser_PublicIdAndPrimaryIsTrue(UUID userPublicId);

    Optional<Address> findFirstByUser_PublicIdOrderByCreatedAtAsc(UUID userPublicId);

    boolean existsByUserId(Long userId);

}
