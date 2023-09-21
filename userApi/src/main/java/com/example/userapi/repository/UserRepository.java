package com.example.userapi.repository;

import com.example.userapi.model.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    @Query("SELECT u.* FROM users_table u WHERE u.username = :username")
    Mono<User> findUserByUsername(@Param("username") String username);


}
