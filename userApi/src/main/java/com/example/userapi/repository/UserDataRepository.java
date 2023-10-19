package com.example.userapi.repository;

import com.example.userapi.enums.UserDataProfileType;
import com.example.userapi.model.UserData;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserDataRepository extends ReactiveCrudRepository<UserData, Long> {
    @Query("SELECT ud.* FROM users_data_table ud WHERE ud.user_id = :user_id")
    Mono<UserData> findByUserId(@Param("user_id") long userId);

    @Modifying
    @Query("UPDATE users_data_table SET server_seed = :serverSeed WHERE user_id = :userId")
    Mono<Void> updateServerSeedByUserId(@Param("serverSeed") String serverSeed,
                                            @Param("userId") long userId);
    @Modifying
    @Query("UPDATE users_data_table SET client_seed = :clientSeed WHERE user_id = :userId")
    Mono<Void> updateClientSeedByUserId(@Param("serverSeed") String clientSeed,
                                          @Param("userId") long userId);

    @Modifying
    @Query("UPDATE users_data_table SET profile_type = :profileType WHERE user_id = :userId")
    Mono<Void> updateProfileTypeByUserId(@Param("profileType") UserDataProfileType profileType,
                                     @Param("userId") long userId);

    @Modifying
    @Query("UPDATE users_data_table SET balance = :balance WHERE user_id = :userId")
    Mono<Void> updateBalanceByUserId(@Param("balance") long balance,
                                    @Param("userId") long userId);

}
