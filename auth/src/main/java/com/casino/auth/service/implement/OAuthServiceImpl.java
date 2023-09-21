package com.casino.auth.service.implement;

import com.casino.auth.dto.google.GoogleAccessTokenDto;
import com.casino.auth.dto.google.GoogleAccessTokenRequest;
import com.casino.auth.dto.google.GoogleCertsDto;
import com.casino.auth.dto.vk.VkAccessTokenDto;
import com.casino.auth.dto.vk.VkUserDto;
import com.casino.auth.enums.UserDataProfileType;
import com.casino.auth.enums.UserDataRank;
import com.casino.auth.enums.UserDataRole;
import com.casino.auth.mapper.GoogleAuthorizationRequestToUserActivityMapperImpl;
import com.casino.auth.mapper.GoogleAuthorizationRequestToUserMapperImpl;
import com.casino.auth.mapper.VkAuthorizationRequestToUserActivityMapperImpl;
import com.casino.auth.mapper.VkAuthorizationRequestToUserMapperImpl;
import com.casino.auth.model.User;
import com.casino.auth.model.UserActivity;
import com.casino.auth.model.UserData;
import com.casino.auth.payload.AuthorizationResponse;
import com.casino.auth.payload.google.GoogleAuthorizationRequest;
import com.casino.auth.payload.vk.VkAuthorizationRequest;
import com.casino.auth.property.GoogleOAuthProperties;
import com.casino.auth.property.VkOAuthProperties;
import com.casino.auth.security.jwt.JwtUtil;
import com.casino.auth.service.OAuthService;
import com.casino.auth.util.HexGeneratorUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;

@Service
public class OAuthServiceImpl implements OAuthService {
    private static final String GET_VK_USER_URL = "https://api.vk.com/method/users.get";
    private static final String  GET_GOOGLE_ACCESS_TOKEN_URL = "https://www.googleapis.com/oauth2/v4/token";
    private static final String GET_VK_ACCESS_CODE_URL = "https://oauth.vk.com/access_token";
    private static final String GET_GOOGLE_CERTS_URL = "https://www.googleapis.com/oauth2/v3/certs";
    private static final String SAVE_USER_URL = "http://localhost:8081/api/user/save";
    private static final String EXISTS_BY_USERNAME_URL = "http://localhost:8081/api/user/existsByUsername";
    private static final String SAVE_USER_ACTIVITY_URL = "http://localhost:8081/api/userActivity/save";
    private static final String SAVE_USER_DATA_URL = "http://localhost:8081/api/userData/save";
    private static final String FIND_ORIGINAL_USER_BY_USERNAME_URL = "http://localhost:8081/api/user/findOriginalUserByUsername";
    private final String OAUTH_REDIRECT_VK_URL;
    private final String OAUTH_REDIRECT_GOOGLE_URL;
    private final VkOAuthProperties vkOAuthProperties;
    private final GoogleOAuthProperties googleOAuthProperties;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final VkAuthorizationRequestToUserMapperImpl vkAuthorizationRequestToUserMapper;
    private final VkAuthorizationRequestToUserActivityMapperImpl vkAuthorizationRequestToUserActivityMapper;
    private final GoogleAuthorizationRequestToUserMapperImpl googleAuthorizationRequestToUserMapper;
    private final GoogleAuthorizationRequestToUserActivityMapperImpl googleAuthorizationRequestToUserActivityMapper;

    @Autowired
    public OAuthServiceImpl(VkOAuthProperties vkOAuthProperties,
                            VkAuthorizationRequestToUserMapperImpl vkAuthorizationRequestToUserMapper,
                            VkAuthorizationRequestToUserActivityMapperImpl vkAuthorizationRequestToUserActivityMapper,
                            JwtUtil jwtUtil,
                            BCryptPasswordEncoder bCryptPasswordEncoder,
                            GoogleOAuthProperties googleOAuthProperties,
                            GoogleAuthorizationRequestToUserMapperImpl googleAuthorizationRequestToUserMapper,
                            GoogleAuthorizationRequestToUserActivityMapperImpl googleAuthorizationRequestToUserActivityMapper
                            ) {
        this.vkOAuthProperties = vkOAuthProperties;
        this.vkAuthorizationRequestToUserMapper  = vkAuthorizationRequestToUserMapper;
        this.googleAuthorizationRequestToUserMapper = googleAuthorizationRequestToUserMapper;
        this.vkAuthorizationRequestToUserActivityMapper = vkAuthorizationRequestToUserActivityMapper;
        this.jwtUtil = jwtUtil;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.googleOAuthProperties = googleOAuthProperties;
        this.googleAuthorizationRequestToUserActivityMapper = googleAuthorizationRequestToUserActivityMapper;
        this.OAUTH_REDIRECT_VK_URL = "https://oauth.vk.com/authorize?client_id="
                + vkOAuthProperties.getClientId() + "&display=page&redirect_uri="
                + vkOAuthProperties.getRedirectUrl() + "&scope="
                + vkOAuthProperties.getScopes() + "&response_type=code&v=5.131&state=123456";
        this.OAUTH_REDIRECT_GOOGLE_URL = "https://accounts.google.com/o/oauth2/auth?client_id="
                + googleOAuthProperties.getClientId() + "&redirect_uri="
                + googleOAuthProperties.getRedirectUrl() + "&scope="
                + googleOAuthProperties.getScopes() + "&response_type=code";
    }
    @Override
    public Mono<AuthorizationResponse> handleAuthorizeByVk(VkAuthorizationRequest vkAuthorizationRequest) {
        return getVKAccessTokenByCode(vkAuthorizationRequest.getCode())
                .flatMap(vkAccessTokenDto -> getVKUserByAccessToken(vkAccessTokenDto.getAccessToken()))
                .flatMap(vkUserDto -> {
                    String username = vkUserDto.getResponse()[0].getId() + "_vk";

                    return existsByUsername(username)
                            .flatMap(userExists -> {
                                if (userExists) {
                                    return findOriginalUserByUsername(username)
                                            .flatMap(user -> {
                                                long userId = user.getId();

                                                UserActivity userActivity =
                                                        vkAuthorizationRequestToUserActivityMapper
                                                                .vkAuthorizationRequestToUserActivity(vkAuthorizationRequest);

                                                userActivity.setUserId(userId);

                                                return saveUserActivity(userActivity)
                                                        .then(jwtUtil.generateToken(user).map(AuthorizationResponse::new));
                                            });
                                } else {
                                    User user = vkAuthorizationRequestToUserMapper
                                            .vkAuthorizationRequestToUser(vkAuthorizationRequest);

                                    user.setUsername(username);
                                    user.setPassword(bCryptPasswordEncoder.encode(HexGeneratorUtil.generateHex()));

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
                                                        UserDataProfileType.PUBLIC,
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
    public Mono<AuthorizationResponse> handleAuthorizeByGoogle(GoogleAuthorizationRequest googleAuthorizationRequest) {
        Mono<GoogleAccessTokenDto> googleAccessTokenDtoMono =
                getGoogleAccessTokenDtoByGoogleAccessTokenRequest(
                        new GoogleAccessTokenRequest(
                                googleOAuthProperties.getClientId(),
                                googleOAuthProperties.getClientSecret(),
                                googleOAuthProperties.getRedirectUrl(),
                                URLDecoder.decode(googleAuthorizationRequest.getCode(), StandardCharsets.UTF_8),
                                "authorization_code"
                        )
                );

        Mono<GoogleCertsDto> googleCertsDtoMono =
                getGoogleCerts();

        return Mono.zip(googleAccessTokenDtoMono, googleCertsDtoMono)
                .flatMap(tuple -> {
                    List<GoogleCertsDto.Key> keyList =
                            tuple.getT2().getKeys();

                    return Flux.fromIterable(keyList)
                            .flatMap(key -> {
                                try {
                                    BigInteger modulusBigInt = new BigInteger(1, Base64.getUrlDecoder().decode(key.getN()));
                                    BigInteger exponentBigInt = new BigInteger(1, Base64.getUrlDecoder().decode(key.getE()));

                                    RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulusBigInt, exponentBigInt);
                                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                                    PublicKey publicKey = keyFactory.generatePublic(keySpec);

                                    Jwt<?, ?> jwtd = Jwts.parserBuilder()
                                            .setSigningKey(publicKey)
                                            .build()
                                            .parse(tuple.getT1().getIdToken());

                                    Claims claims = (Claims) jwtd.getBody();

                                    return Mono.just(claims);

                                } catch (NoSuchAlgorithmException exception1) {
                                    return Mono.error(new NoSuchAlgorithmException());
                                } catch (InvalidKeySpecException invalidKeySpecException){
                                    return Mono.error(new InvalidKeySpecException());
                                }

                            })
                            .onErrorContinue((error, message) -> {})
                            .next();
                })
                .flatMap(claims -> {
                    String username = claims.get("sub", String.class) + "_google";

                    return existsByUsername(username)
                            .flatMap(exists -> {
                                if (exists){
                                    return findOriginalUserByUsername(username)
                                            .flatMap(user -> {
                                        long userId = user.getId();

                                        UserActivity userActivity =
                                                googleAuthorizationRequestToUserActivityMapper
                                                        .googleAuthorizationRequestToUserActivity(googleAuthorizationRequest);

                                        userActivity.setUserId(userId);

                                        return saveUserActivity(userActivity)
                                                        .then(jwtUtil.generateToken(user).map(AuthorizationResponse::new));
                                    });
                                } else {
                                    String nickname;

                                    String givenName = claims.get("given_name", String.class);
                                    String familyName = null;

                                    try {
                                        familyName = claims.get("family_name", String.class);
                                    }catch (Exception ignore){
                                    }

                                    if (familyName != null){
                                        nickname = givenName + " " + familyName.toCharArray()[0] + ".";
                                    } else {
                                        nickname = givenName;
                                    }

                                    User user = googleAuthorizationRequestToUserMapper
                                            .googleAuthorizationRequestToUser(googleAuthorizationRequest);

                                    user.setUsername(username);
                                    user.setPassword(bCryptPasswordEncoder.encode(HexGeneratorUtil.generateHex()));


                                    return saveUser(user)
                                            .flatMap(savedUser -> {
                                                long userId = savedUser.getId();

                                                UserActivity userActivity =
                                                        googleAuthorizationRequestToUserActivityMapper
                                                                .googleAuthorizationRequestToUserActivity(googleAuthorizationRequest);

                                                userActivity.setUserId(userId);

                                                UserData userData = new UserData(
                                                        null,
                                                        userId,
                                                        UserDataProfileType.PUBLIC,
                                                        nickname,
                                                        0L,
                                                        UserDataRole.DEFAULT,
                                                        UserDataRank.NEWBIE,
                                                        HexGeneratorUtil.generateHex(),
                                                        HexGeneratorUtil.generateHex()
                                                );

                                                return Mono.zip(saveUserActivity(userActivity), saveUserData(userData))
                                                        .then(jwtUtil.generateToken(savedUser).map(AuthorizationResponse::new));
                                            });
                                }
                            });
                });
    }



    @Override
    public Mono<ResponseEntity<Void>> handleRedirectVK() {
        return Mono.fromSupplier(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(OAUTH_REDIRECT_VK_URL));

            return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
        });
    }

    @Override
    public Mono<ResponseEntity<Void>> handleRedirectGoogle() {
        return Mono.fromSupplier(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(OAUTH_REDIRECT_GOOGLE_URL));

            return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
        });
    }


    private Mono<VkAccessTokenDto> getVKAccessTokenByCode(String code){
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

    private Mono<VkUserDto> getVKUserByAccessToken(String accessToken){
        return WebClient.builder()
                .baseUrl(GET_VK_USER_URL + "?v=5.131&fields=has_photo,photo_max")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .build()
                .get()
                .retrieve()
                .bodyToMono(VkUserDto.class);
    }

    private Mono<GoogleAccessTokenDto> getGoogleAccessTokenDtoByGoogleAccessTokenRequest(GoogleAccessTokenRequest googleAccessTokenRequest){
        WebClient.Builder webClient = WebClient.builder();
        return webClient
                .baseUrl(GET_GOOGLE_ACCESS_TOKEN_URL)
                .build()
                .post()
                .bodyValue(googleAccessTokenRequest)
                .retrieve()
                .bodyToMono(GoogleAccessTokenDto.class);
    }

    private Mono<GoogleCertsDto> getGoogleCerts(){
        WebClient.Builder webClient = WebClient.builder();
        return webClient
                .baseUrl(GET_GOOGLE_CERTS_URL)
                .build()
                .get()
                .retrieve()
                .bodyToMono(GoogleCertsDto.class);
    }


    private Mono<User> findOriginalUserByUsername(String username){
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
