package com.example.userapi.repository;

import com.example.userapi.model.UserActivity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface UserActivityRepository extends ReactiveCrudRepository<UserActivity, Long> {
    @Query("SELECT ua.* FROM users_activity_table ua WHERE ua.user_id = :user_id ORDER BY ua.timestamp DESC LIMIT 20")
    Flux<UserActivity> findAllByUserId(@Param("user_id") long userId);
}
