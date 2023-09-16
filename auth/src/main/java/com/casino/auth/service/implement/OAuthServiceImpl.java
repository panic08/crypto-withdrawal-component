package com.casino.auth.service.implement;

import com.casino.auth.dto.vk.VkAccessTokenDto;
import com.casino.auth.dto.vk.VkUserDto;
import com.casino.auth.enums.UserDataRank;
import com.casino.auth.enums.UserDataRole;
import com.casino.auth.mapper.VkAuthorizationRequestToUserActivityMapperImpl;
import com.casino.auth.model.User;
import com.casino.auth.model.UserActivity;
import com.casino.auth.model.UserData;
import com.casino.auth.payload.AuthorizationResponse;
import com.casino.auth.payload.vk.VkAuthorizationRequest;
import com.casino.auth.property.VkOAuthProperties;
import com.casino.auth.security.jwt.JwtUtil;
import com.casino.auth.service.OAuthService;
import com.casino.auth.util.HexGeneratorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;

@Service
public class OAuthServiceImpl implements OAuthService {
    private static final String GET_VK_USER_URL = "https://api.vk.com/method/users.get";
    private static final String GET_VK_ACCESS_CODE_URL = "https://oauth.vk.com/access_token";
    private static final String SAVE_USER_URL = "http://localhost:8081/api/user/save";
    private static final String EXISTS_BY_USERNAME_URL = "http://localhost:8081/api/user/existsByUsername";
    private static final String SAVE_USER_ACTIVITY_URL = "http://localhost:8081/api/userActivity/save";
    private static final String SAVE_USER_DATA_URL = "http://localhost:8081/api/userData/save";
    private static final String FIND_ORIGINAL_USER_BY_USERNAME_URL = "http://localhost:8081/api/user/findOriginalUserByUsername";
    private final String OAUTH_REDIRECT_VK_URL;
    private final VkOAuthProperties vkOAuthProperties;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final VkAuthorizationRequestToUserActivityMapperImpl vkAuthorizationRequestToUserActivityMapper;

    @Autowired
    public OAuthServiceImpl(VkOAuthProperties vkOAuthProperties,
                            VkAuthorizationRequestToUserActivityMapperImpl vkAuthorizationRequestToUserActivityMapper,
                            JwtUtil jwtUtil,
                            BCryptPasswordEncoder bCryptPasswordEncoder
                            ) {
        this.vkOAuthProperties = vkOAuthProperties;
        this.vkAuthorizationRequestToUserActivityMapper = vkAuthorizationRequestToUserActivityMapper;
        this.jwtUtil = jwtUtil;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.OAUTH_REDIRECT_VK_URL = "https://oauth.vk.com/authorize?client_id="
                + vkOAuthProperties.getClientId() + "&display=page&redirect_uri="
                + vkOAuthProperties.getRedirectUrl() + "&scope="
                + vkOAuthProperties.getScopes() + "&response_type=code&v=5.131&state=123456";
    }
    @Override
    public Mono<AuthorizationResponse> handleAuthorizeByVk(VkAuthorizationRequest vkAuthorizationRequest) {
        return getAccessTokenByCode(vkAuthorizationRequest.getCode())
                .flatMap(vkAccessTokenDto -> getUserByAccessToken(vkAccessTokenDto.getAccessToken()))
                .flatMap(vkUserDto -> {
                    String username = vkUserDto.getResponse()[0].getId() + "_vk";

                    return existsByUsername(username)
                            .flatMap(userExists -> {
                                if (userExists) {
                                    return findUserByUsername(username).flatMap(user -> jwtUtil.generateToken(user).map(AuthorizationResponse::new));
                                } else {
                                    User user = new User();
                                    user.setUsername(username);
                                    user.setPassword(bCryptPasswordEncoder.encode(HexGeneratorUtil.generateHex()));
                                    user.setIsAccountNonLocked(true);
                                    user.setRegisteredAt(System.currentTimeMillis());

                                    return saveUser(user)
                                            .flatMap(savedUser -> {
                                                long userId = savedUser.getId();

                                                String nickname = vkUserDto.getResponse()[0].getFirstName()
                                                        + " "
                                                        + vkUserDto.getResponse()[0].getLastName().toCharArray()[0] + ".";

                                                UserActivity userActivity =
                                                        vkAuthorizationRequestToUserActivityMapper
                                                                .vkAuthorizationRequestToUserActivity(vkAuthorizationRequest);

                                                userActivity.setUserId(userId);

                                                UserData userData = new UserData(
                                                        null,
                                                        userId,
                                                        nickname,
                                                        0L,
                                                        UserDataRole.DEFAULT,
                                                        UserDataRank.NEWBIE,
                                                        HexGeneratorUtil.generateHex(),
                                                        HexGeneratorUtil.generateHex()
                                                );

                                                return Mono.zip(
                                                                saveUserActivity(userActivity),
                                                                saveUserData(userData)
                                                        )
                                                        .then(jwtUtil.generateToken(savedUser).map(AuthorizationResponse::new));
                                            });
                                }
                            });
                });
    }


    @Override
    public Mono<ResponseEntity<Void>> handleRedirectVK() {
        return Mono.fromCallable(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(OAUTH_REDIRECT_VK_URL));

            return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
        });
    }

    private Mono<VkAccessTokenDto> getAccessTokenByCode(String code){
        WebClient.Builder webClient = WebClient.builder();
        return webClient
                .baseUrl(GET_VK_ACCESS_CODE_URL + "?client_id="
                        + vkOAuthProperties.getClientId()
                        + "&client_secret=" + vkOAuthProperties.getClientSecret()
                        + "&redirect_uri=" + vkOAuthProperties.getRedirectUrl()
                        + "&code=" + code
                )
                .build()
                .post()
                .retrieve()
                .bodyToMono(VkAccessTokenDto.class);
    }

    private Mono<VkUserDto> getUserByAccessToken(String accessToken){
        return WebClient.builder()
                .baseUrl(GET_VK_USER_URL + "?v=5.131&fields=has_photo,photo_max")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .build()
                .get()
                .retrieve()
                .bodyToMono(VkUserDto.class);
    }

    private Mono<User> findUserByUsername(String username){
        return WebClient.builder()
                .baseUrl(FIND_ORIGINAL_USER_BY_USERNAME_URL + "?username=" + username)
                .build()
                .get()
                .retrieve()
                .bodyToMono(User.class);
    }
    private Mono<User> saveUser(User user){
        return WebClient.builder()
                .baseUrl(SAVE_USER_URL)
                .build()
                .post()
                .bodyValue(user)
                .retrieve()
                .bodyToMono(User.class)
                .cache();
    }

    private Mono<UserData> saveUserData(UserData userData){
        return WebClient.builder()
                .baseUrl(SAVE_USER_DATA_URL)
                .build()
                .post()
                .bodyValue(userData)
                .retrieve()
                .bodyToMono(UserData.class)
                .cache();
    }

    private Mono<UserActivity> saveUserActivity(UserActivity userActivity){
        return WebClient.builder()
                .baseUrl(SAVE_USER_ACTIVITY_URL)
                .build()
                .post()
                .bodyValue(userActivity)
                .retrieve()
                .bodyToMono(UserActivity.class)
                .cache();
    }

    private Mono<Boolean> existsByUsername(String username){
        return WebClient.builder()
                .baseUrl(EXISTS_BY_USERNAME_URL + "?username=" + username)
                .build()
                .get()
                .retrieve()
                .bodyToMono(Boolean.class);
    }
}
