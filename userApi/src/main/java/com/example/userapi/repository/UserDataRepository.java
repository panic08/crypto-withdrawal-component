package com.example.userapi.repository;

import com.example.userapi.model.UserData;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserDataRepository extends ReactiveCrudRepository<UserData, Long> {
    @Query("SELECT DISTINCT ud.* FROM users_data_table ud WHERE ud.user_id = :user_id")
    Mono<UserData> findByUserId(@Param("user_id") Long userId);
}
