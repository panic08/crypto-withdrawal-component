package com.casino.auth.service.implement;

import com.casino.auth.dto.UserDto;
import com.casino.auth.enums.UserDataRank;
import com.casino.auth.enums.UserDataRole;
import com.casino.auth.exception.InvalidCredentialsException;
import com.casino.auth.exception.UserAlreadyExistsException;
import com.casino.auth.mapper.AuthorizationRequestToUserActivityMapperImpl;
import com.casino.auth.mapper.AuthorizationRequestToUserMapperImpl;
import com.casino.auth.model.User;
import com.casino.auth.model.UserActivity;
import com.casino.auth.model.UserData;
import com.casino.auth.payload.AuthorizationRequest;
import com.casino.auth.payload.AuthorizationResponse;
import com.casino.auth.security.jwt.JwtUtil;
import com.casino.auth.service.AuthorizationService;
import com.casino.auth.util.HexGeneratorUtil;
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
    private static final String EXISTS_BY_USERNAME_URL = "http://localhost:8081/api/user/existsByUsername";
    private static final String SAVE_USER_URL = "http://localhost:8081/api/user/save";
    private static final String SAVE_USER_ACTIVITY_URL = "http://localhost:8081/api/userActivity/save";
    private static final String SAVE_USER_DATA_URL = "http://localhost:8081/api/userData/save";
    private static final String FIND_ORIGINAL_USER_BY_USERNAME_URL = "http://localhost:8081/api/user/findOriginalUserByUsername";
    private static final String FIND_USER_BY_ID_URL = "http://localhost:8081/api/user/findUserById";


    @Override
    public Mono<AuthorizationResponse> handleRegister(AuthorizationRequest authorizationRequest) {
        return Mono.fromCallable(() -> {
            User user = authorizationRequestToUserMapper
                    .authorizationRequestToUser(authorizationRequest);

            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

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
                                        username,
                                        0L,
                                        UserDataRole.DEFAULT,
                                        UserDataRank.NEWBIE,
                                        HexGeneratorUtil.generateHex(),
                                        HexGeneratorUtil.generateHex()
                                );


                                return Mono.zip(
                                        saveUserActivity(userActivity),
                                        saveUserData(userData)
                                ).then(jwtUtil.generateToken(savedUser)).map(AuthorizationResponse::new);
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
        }).flatMap(objects -> jwtUtil.generateToken(objects.getT1()).map(AuthorizationResponse::new));
    }

    @Override
    public Mono<UserDto> getInfoByToken(String token) {
        return jwtUtil.extractId(token)
                .flatMap(this::findUserById)
                .onErrorResume(ex -> Mono.error(new InvalidCredentialsException("Incorrect token")));
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
