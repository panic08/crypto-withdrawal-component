package com.casino.replenishments.api;

import com.casino.replenishments.enums.UserDataProfileType;
import com.casino.replenishments.model.User;
import com.casino.replenishments.model.UserActivity;
import com.casino.replenishments.model.UserCombined;
import com.casino.replenishments.model.UserData;
import com.casino.replenishments.property.ServicesIpProperty;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserApi {
    private final ServicesIpProperty servicesIpProperty;
    private final WebClient.Builder webClient;
    private static String EXISTS_USER_BY_USERNAME_URL;
    private static String SAVE_USER_URL;
    private static String SAVE_USER_ACTIVITY_URL;
    private static String SAVE_USER_DATA_URL;
    private static String FIND_USER_BY_USERNAME_URL;
    private static String FIND_USER_BY_ID_URL;
    private static String FIND_USERCOMBINED_BY_ID_URL;
    private static String UPDATE_SERVER_SEED_BY_USERID;
    private static String UPDATE_CLIENT_SEED_BY_USERID;
    private static String UPDATE_PROFILE_TYPE_BY_USERID;
    private static String UPDATE_USER_ACCOUNT_NON_LOCKED_BY_ID;
    private static String UPDATE_USERDATA_BALANCE_BY_USERID;


    @PostConstruct
    public void init() {
        EXISTS_USER_BY_USERNAME_URL = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/user/existsByUsername";
        SAVE_USER_URL = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/user/save";
        SAVE_USER_ACTIVITY_URL = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/userActivity/save";
        SAVE_USER_DATA_URL = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/userData/save";
        FIND_USER_BY_USERNAME_URL = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/user/findUserByUsername";
        FIND_USERCOMBINED_BY_ID_URL = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/userCombined/findUserCombinedById";
        FIND_USER_BY_ID_URL = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/user/findUserById";
        UPDATE_SERVER_SEED_BY_USERID = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/userData/updateServerSeedByUserId";
        UPDATE_CLIENT_SEED_BY_USERID = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/userData/updateClientSeedByUserId";
        UPDATE_PROFILE_TYPE_BY_USERID = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/userData/updateProfileTypeByUserId";
        UPDATE_USERDATA_BALANCE_BY_USERID = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/userData/updateBalanceByUserId";
        UPDATE_USER_ACCOUNT_NON_LOCKED_BY_ID = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/user/updateAccountNonLockedById";

    }
    public Mono<User> findUserByUsername(String username){
        return webClient
                .baseUrl(FIND_USER_BY_USERNAME_URL + "?username=" + username)
                .build()
                .get()
                .retrieve()
                .bodyToMono(User.class);
    }

    public Mono<Boolean> existsByUsername(String username){
        return webClient
                .baseUrl(EXISTS_USER_BY_USERNAME_URL + "?username=" + username)
                .build()
                .get()
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    public Mono<User> saveUser(User user){
        return webClient
                .baseUrl(SAVE_USER_URL)
                .build()
                .post()
                .bodyValue(user)
                .retrieve()
                .bodyToMono(User.class)
                .cache();
    }

    public Mono<UserCombined> findUserCombinedById(long id){
        return webClient
                .baseUrl(FIND_USERCOMBINED_BY_ID_URL + "?id=" + id)
                .build()
                .get()
                .retrieve()
                .bodyToMono(UserCombined.class);
    }

    public Mono<UserData> saveUserData(UserData userData){
        return webClient
                .baseUrl(SAVE_USER_DATA_URL)
                .build()
                .post()
                .bodyValue(userData)
                .retrieve()
                .bodyToMono(UserData.class)
                .cache();
    }

    public Mono<User> findUserById(long id){
        return webClient
                .baseUrl(FIND_USER_BY_ID_URL + "?id=" + id)
                .build()
                .get()
                .retrieve()
                .bodyToMono(User.class);
    }

    public Mono<UserActivity> saveUserActivity(UserActivity userActivity){
        return webClient
                .baseUrl(SAVE_USER_ACTIVITY_URL)
                .build()
                .post()
                .bodyValue(userActivity)
                .retrieve()
                .bodyToMono(UserActivity.class)
                .cache();
    }
    public Mono<Void> updateServerSeedByUserId(long userId, String serverSeed){
        return webClient.baseUrl(UPDATE_SERVER_SEED_BY_USERID + "?userId=" + userId
                        + "&serverSeed=" + serverSeed)
                .build()
                .put()
                .retrieve()
                .bodyToMono(Void.class)
                .cache();
    }

    public Mono<Void> updateClientSeedByUserId(long userId, String clientSeed){
        return webClient.baseUrl(UPDATE_CLIENT_SEED_BY_USERID + "?userId=" + userId
                        + "&clientSeed=" + clientSeed)
                .build()
                .put()
                .retrieve()
                .bodyToMono(Void.class)
                .cache();
    }

    public Mono<Void> updateProfileTypeByUserId(long userId, UserDataProfileType profileType){
        return webClient.baseUrl(UPDATE_PROFILE_TYPE_BY_USERID + "?userId=" + userId
                        + "&profileType=" + profileType)
                .build()
                .put()
                .retrieve()
                .bodyToMono(Void.class)
                .cache();
    }

    public Mono<Void> updateUserDataBalanceByUserId(long balance, long userId){
        return webClient.baseUrl(UPDATE_USERDATA_BALANCE_BY_USERID + "?userId=" + userId
                        + "&balance=" + balance)
                .build()
                .put()
                .retrieve()
                .bodyToMono(Void.class)
                .cache();
    }
    public Mono<Void> updateUserAccountNonLockedById(boolean isAccountNonLocked, long id){
        return webClient.baseUrl(UPDATE_USER_ACCOUNT_NON_LOCKED_BY_ID + "?id=" + id
                        + "&accountNonLocked=" + isAccountNonLocked)
                .build()
                .put()
                .retrieve()
                .bodyToMono(Void.class)
                .cache();
    }
}
