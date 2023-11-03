package com.casino.withdrawals.repository;

import com.casino.withdrawals.model.Withdrawal;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WithdrawalRepository extends ReactiveCrudRepository<Withdrawal, Long> {
}
