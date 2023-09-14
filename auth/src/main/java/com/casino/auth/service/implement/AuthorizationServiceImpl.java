package com.casino.auth.service.implement;

import com.casino.auth.dto.UserDto;
import com.casino.auth.enums.CryptoDataType;
import com.casino.auth.enums.UserDataRank;
import com.casino.auth.enums.UserDataRole;
import com.casino.auth.exception.InvalidCredentialsException;
import com.casino.auth.mapper.AuthorizationRequestToUserActivityMapperImpl;
import com.casino.auth.mapper.AuthorizationRequestToUserMapperImpl;
import com.casino.auth.model.CryptoData;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private final JwtUtil jwtUtil;
    private final WebClient.Builder webClient;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthorizationRequestToUserMapperImpl authorizationRequestToUserMapper;
    private final AuthorizationRequestToUserActivityMapperImpl authorizationRequestToUserActivityMapper;
    private static final String EXISTS_BY_USERNAME_URL = "http://localhost:8081/api/user/existsByUsername?username=";
    private static final String FIND_ORIGINAL_USER_BY_USERNAME_URL = "http://localhost:8081/api/user/findOriginalUserByUsername?username=";

    @Override
    public Mono<AuthorizationResponse> handleRegister(AuthorizationRequest authorizationRequest) {
        return Mono.fromCallable(() -> {
            User user = authorizationRequestToUserMapper
                    .authorizationRequestToUser(authorizationRequest);

            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

            return user;
        }).flatMap(user -> webClient
                .baseUrl(EXISTS_BY_USERNAME_URL + user.getUsername())
                .build()
                .get()
                .retrieve()
                .bodyToMono(Boolean.class)
                .flatMap(savedNonSuccessfully -> {
                    if (savedNonSuccessfully){
                        return Mono.error(new InvalidCredentialsException("Invalid login or password"));
                    }

                    return saveUser(user)
                            .flatMap(savedUser -> {
                                long userId = savedUser.getId();
                                String username = savedUser.getUsername();

                                Flux<CryptoData> cryptoDataFlux = generateCryptoData();

                                cryptoDataFlux = cryptoDataFlux.map(cryptoData -> {
                                    cryptoData.setUserId(userId);

                                    return cryptoData;
                                });

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
                                        saveAllCryptoData(cryptoDataFlux).collectList(),
                                        saveUserActivity(userActivity),
                                        saveUserData(userData)
                                );
                            }).flatMap(objects -> jwtUtil.generateToken(user)).map(AuthorizationResponse::new);
                }));
    }

    @Override
    public Mono<AuthorizationResponse> handleLogin(AuthorizationRequest authorizationRequest) {
        Mono<User> userMono = webClient
                .baseUrl(FIND_ORIGINAL_USER_BY_USERNAME_URL + authorizationRequest.getUsername())
                .build()
                .get()
                .retrieve()
                .bodyToMono(User.class);
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
        return jwtUtil.extractUsername(token)
                .flatMap(s -> webClient
                        .baseUrl("http://localhost:8081/api/user/findUserByUsername?username=" + s)
                        .build()
                        .get()
                        .retrieve()
                        .bodyToMono(UserDto.class)
                );
    }

    private Mono<User> saveUser(User user){
        return webClient
                .baseUrl("http://localhost:8081/api/user/save")
                .build()
                .post()
                .bodyValue(user)
                .retrieve()
                .bodyToMono(User.class)
                .cache();
    }

    private Flux<CryptoData> saveAllCryptoData(Flux<CryptoData> cryptoDataFlux) {
        return cryptoDataFlux.collectList()
                .flatMapMany(cryptoDataList -> webClient
                        .baseUrl("http://localhost:8081/api/cryptoData/saveAll")
                        .build()
                        .post()
                        .bodyValue(cryptoDataList)
                        .retrieve()
                        .bodyToFlux(CryptoData.class));
    }



    private Mono<UserData> saveUserData(UserData userData){
        return webClient
                .baseUrl("http://localhost:8081/api/userData/save")
                .build()
                .post()
                .bodyValue(userData)
                .retrieve()
                .bodyToMono(UserData.class)
                .cache();
    }

    private Mono<UserActivity> saveUserActivity(UserActivity userActivity){
        return webClient
                .baseUrl("http://localhost:8081/api/userActivity/save")
                .build()
                .post()
                .bodyValue(userActivity)
                .retrieve()
                .bodyToMono(UserActivity.class)
                .cache();
    }
    private Flux<CryptoData> generateCryptoData(){
        return Flux.just(new CryptoData(null, null, CryptoDataType.BTC, "bci203iri0df0sfi", "pfosdfosdfods", "fdksjfoksdko"));
    }
}
