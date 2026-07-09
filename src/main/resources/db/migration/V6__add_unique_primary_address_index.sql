CREATE UNIQUE INDEX uq_addresses_one_primary_per_user
    ON addresses (user_id)
    WHERE is_primary = true;