package com.casino.auth.service.implement;

import com.casino.auth.dto.UserDto;
import com.casino.auth.enums.UserDataProfileType;
import com.casino.auth.enums.UserDataRank;
import com.casino.auth.enums.UserRole;
import com.casino.auth.exception.IncorrectTokenProvidedException;
import com.casino.auth.exception.InvalidCredentialsException;
import com.casino.auth.exception.UserAlreadyExistsException;
import com.casino.auth.mapper.AuthorizationRequestToUserActivityMapperImpl;
import com.casino.auth.mapper.AuthorizationRequestToUserMapperImpl;
import com.casino.auth.model.User;
import com.casino.auth.model.UserActivity;
import com.casino.auth.model.UserData;
import com.casino.auth.payload.AuthorizationRequest;
import com.casino.auth.payload.AuthorizationResponse;
import com.casino.auth.property.ServicesIpProperty;
import com.casino.auth.security.jwt.JwtUtil;
import com.casino.auth.service.AuthorizationService;
import com.casino.auth.util.HexGeneratorUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private final JwtUtil jwtUtil;
    private final WebClient.Builder webClient;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthorizationRequestToUserMapperImpl authorizationRequestToUserMapper;
    private final AuthorizationRequestToUserActivityMapperImpl authorizationRequestToUserActivityMapper;
    private final ServicesIpProperty servicesIpProperty;
    private static String EXISTS_BY_USERNAME_URL;
    private static String SAVE_USER_URL;
    private static String SAVE_USER_ACTIVITY_URL;
    private static String SAVE_USER_DATA_URL;
    private static String FIND_ORIGINAL_USER_BY_USERNAME_URL;
    private static String FIND_ORIGINAL_USER_BY_ID_URL;
    private static String FIND_USER_BY_ID_URL;

    @PostConstruct
    public void init() {
        EXISTS_BY_USERNAME_URL = "http://"
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
        FIND_ORIGINAL_USER_BY_USERNAME_URL = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/user/findOriginalUserByUsername";
        FIND_USER_BY_ID_URL = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/user/findUserById";
        FIND_ORIGINAL_USER_BY_ID_URL = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/user/findOriginalUserById";
    }

    @Override
    public Mono<AuthorizationResponse> handleRegister(AuthorizationRequest authorizationRequest) {
        return Mono.fromCallable(() -> {
            User user = authorizationRequestToUserMapper
                    .authorizationRequestToUser(authorizationRequest);

            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            user.setRole(UserRole.DEFAULT);

            return user;
        }).flatMap(user -> existsByUsername(user.getUsername())
                .flatMap(exists-> {
                    if (exists){
                        return Mono.error(new UserAlreadyExistsException("This user already exists"));
                    }

                    return saveUser(user)
                            .flatMap(savedUser -> {
                                long userId = savedUser.getId();
                                String username = savedUser.getUsername();


                                UserActivity userActivity = authorizationRequestToUserActivityMapper
                                        .authorizationRequestToUserActivity(authorizationRequest);

                                userActivity.setUserId(userId);

                                UserData userData = new UserData(
                                        null,
                                        userId,
                                        UserDataProfileType.PUBLIC,
                                        username,
                                        0L,
                                        UserDataRank.NEWBIE,
                                        HexGeneratorUtil.generateHex(),
                                        HexGeneratorUtil.generateHex()
                                );


                                return Mono.zip(
                                        saveUserActivity(userActivity),
                                        saveUserData(userData)
                                ).thenReturn(new AuthorizationResponse(jwtUtil.generateAccessToken(savedUser),
                                        jwtUtil.generateRefreshToken(savedUser)));
                            });
                }));
    }

    @Override
    public Mono<AuthorizationResponse> handleLogin(AuthorizationRequest authorizationRequest) {
        Mono<User> userMono = findOriginalUserByUsername(authorizationRequest.getUsername());

        return userMono.flatMap(user -> {
            if (!bCryptPasswordEncoder.matches(authorizationRequest.getPassword(), user.getPassword())){
                return Mono.error(new InvalidCredentialsException("Incorrect login or password"));
            }

            UserActivity userActivity = authorizationRequestToUserActivityMapper
                    .authorizationRequestToUserActivity(authorizationRequest);

            userActivity.setUserId(user.getId());

            Mono<UserActivity> userActivityMono = saveUserActivity(userActivity);

            return Mono.zip(userMono, userActivityMono);
        }).map(objects -> {
            String accessToken = jwtUtil.generateAccessToken(objects.getT1());
            String refreshToken = jwtUtil.generateRefreshToken(objects.getT1());

            return new AuthorizationResponse(accessToken, refreshToken);
        });
    }

    @Override
    public Mono<UserDto> getUserInfoByAccessToken(String authorization) {
        return Mono.fromCallable(() -> jwtUtil.extractIdFromAccessToken(authorization.split(" ")[1]))
                .onErrorResume(ex -> Mono.error(new IncorrectTokenProvidedException("Incorrect token")))
                .flatMap(this::findUserById);
    }

    @Override
    public Mono<AuthorizationResponse> handleRefreshAccessToken(String authorization) {
        return Mono.fromCallable(() -> jwtUtil.extractIdFromRefreshToken(authorization.split(" ")[1]))
                .onErrorResume(ex -> Mono.error(new IncorrectTokenProvidedException("Incorrect token")))
                .flatMap(this::findOriginalUserById)
                .map(user -> new AuthorizationResponse(jwtUtil.generateAccessToken(user), null));
    }

    private Mono<User> findOriginalUserByUsername(String username){
        return webClient
                .baseUrl(FIND_ORIGINAL_USER_BY_USERNAME_URL + "?username=" + username)
                .build()
                .get()
                .retrieve()
                .bodyToMono(User.class);
    }

    private Mono<Boolean> existsByUsername(String username){
        return webClient
                .baseUrl(EXISTS_BY_USERNAME_URL + "?username=" + username)
                .build()
                .get()
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    private Mono<User> saveUser(User user){
        return webClient
                .baseUrl(SAVE_USER_URL)
                .build()
                .post()
                .bodyValue(user)
                .retrieve()
                .bodyToMono(User.class)
                .cache();
    }

    private Mono<UserDto> findUserById(long id){
        return webClient
                .baseUrl(FIND_USER_BY_ID_URL + "?id=" + id)
                .build()
                .get()
                .retrieve()
                .bodyToMono(UserDto.class);
    }

    private Mono<UserData> saveUserData(UserData userData){
        return webClient
                .baseUrl(SAVE_USER_DATA_URL)
                .build()
                .post()
                .bodyValue(userData)
                .retrieve()
                .bodyToMono(UserData.class)
                .cache();
    }

    private Mono<User> findOriginalUserById(long id){
        return WebClient.builder()
                .baseUrl(FIND_ORIGINAL_USER_BY_ID_URL + "?id=" + id)
                .build()
                .get()
                .retrieve()
                .bodyToMono(User.class);
    }

    private Mono<UserActivity> saveUserActivity(UserActivity userActivity){
        return webClient
                .baseUrl(SAVE_USER_ACTIVITY_URL)
                .build()
                .post()
                .bodyValue(userActivity)
                .retrieve()
                .bodyToMono(UserActivity.class)
                .cache();
    }
}
