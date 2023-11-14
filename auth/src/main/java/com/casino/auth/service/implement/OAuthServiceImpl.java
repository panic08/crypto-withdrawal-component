package com.casino.auth.service.implement;

import com.casino.auth.api.GoogleApi;
import com.casino.auth.api.SteamApi;
import com.casino.auth.api.UserApi;
import com.casino.auth.api.VkApi;
import com.casino.auth.dto.google.GoogleAccessTokenDto;
import com.casino.auth.dto.google.GoogleAccessTokenRequest;
import com.casino.auth.dto.google.GoogleCertsDto;
import com.casino.auth.enums.UserDataProfileType;
import com.casino.auth.enums.UserDataRank;
import com.casino.auth.exception.InvalidCredentialsException;
import com.casino.auth.mapper.*;
import com.casino.auth.model.User;
import com.casino.auth.model.UserActivity;
import com.casino.auth.model.UserData;
import com.casino.auth.payload.AuthorizationResponse;
import com.casino.auth.payload.google.GoogleAuthorizationRequest;
import com.casino.auth.payload.steam.SteamAuthorizationRequest;
import com.casino.auth.payload.vk.VkAuthorizationRequest;
import com.casino.auth.property.GoogleOAuthProperty;
import com.casino.auth.property.SteamOAuthProperty;
import com.casino.auth.property.VkOAuthProperty;
import com.casino.auth.security.jwt.JwtUtil;
import com.casino.auth.service.OAuthService;
import com.casino.auth.util.HexGeneratorUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {
    private final UserApi userApi;
    private final GoogleApi googleApi;
    private final SteamApi steamApi;
    private final VkApi vkApi;
    private final VkOAuthProperty vkOAuthProperty;
    private final GoogleOAuthProperty googleOAuthProperty;
    private final SteamOAuthProperty steamOAuthProperty;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final VkAuthorizationRequestToUserMapperImpl vkAuthorizationRequestToUserMapper;
    private final VkAuthorizationRequestToUserActivityMapperImpl vkAuthorizationRequestToUserActivityMapper;
    private final GoogleAuthorizationRequestToUserMapperImpl googleAuthorizationRequestToUserMapper;
    private final GoogleAuthorizationRequestToUserActivityMapperImpl googleAuthorizationRequestToUserActivityMapper;
    private final SteamAuthorizationRequestToUserActivityMapperImpl steamAuthorizationRequestToUserActivityMapper;
    private final SteamAuthorizationRequestToUserMapperImpl steamAuthorizationRequestToUserMapper;
    private static final String UPLOAD_DIR = System.getProperty("os.name").toLowerCase().contains("linux") ?
            "/srv/photos/" : "D:/photos/";
    private static String OAUTH_REDIRECT_VK_URL;
    private static String OAUTH_REDIRECT_GOOGLE_URL;
    private static String OAUTH_REDIRECT_STEAM_URL;

    @PostConstruct
    public void init() {
        OAUTH_REDIRECT_VK_URL = "https://oauth.vk.com/authorize?client_id="
                + vkOAuthProperty.getClientId() + "&display=page&redirect_uri="
                + vkOAuthProperty.getRedirectUrl() + "&scope="
                + vkOAuthProperty.getScopes() + "&response_type=code&v=5.131&state=123456";
        OAUTH_REDIRECT_GOOGLE_URL = "https://accounts.google.com/o/oauth2/auth?client_id="
                + googleOAuthProperty.getClientId() + "&redirect_uri="
                + googleOAuthProperty.getRedirectUrl() + "&scope="
                + googleOAuthProperty.getScopes() + "&response_type=code";
        OAUTH_REDIRECT_STEAM_URL = "https://steamcommunity.com/openid/login?openid.ns=http://specs.openid.net/auth/2.0&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select&openid.identity=http://specs.openid.net/auth/2.0/identifier_select&openid.return_to="
                + steamOAuthProperty.getRedirectUrl() + "&openid.realm="
                + steamOAuthProperty.getRedirectUrl() + "&openid.mode=checkid_setup";
    }

    @Override
    public Mono<AuthorizationResponse> handleAuthorizeByVk(VkAuthorizationRequest vkAuthorizationRequest) {
        return vkApi.getVKAccessTokenByCode(
                vkOAuthProperty.getClientId(),
                vkOAuthProperty.getClientSecret(),
                vkOAuthProperty.getRedirectUrl(),
                vkAuthorizationRequest.getCode()
                )
                .flatMap(vkAccessTokenDto -> vkApi.getVKUserByAccessToken(vkAccessTokenDto.getAccessToken()))
                .flatMap(vkUserDto -> {
                    String username = vkUserDto.getResponse()[0].getId() + "_vk";

                    return userApi.existsByUsername(username)
                            .flatMap(userExists -> {
                                if (userExists) {
                                    return userApi.findUserByUsername(username)
                                            .flatMap(user -> {
                                                long userId = user.getId();

                                                UserActivity userActivity =
                                                        vkAuthorizationRequestToUserActivityMapper
                                                                .vkAuthorizationRequestToUserActivity(vkAuthorizationRequest);

                                                userActivity.setUserId(userId);

                                                return userApi.saveUserActivity(userActivity)
                                                        .thenReturn(new AuthorizationResponse(jwtUtil.generateAccessToken(user),
                                                                jwtUtil.generateRefreshToken(user)));
                                            });
                                } else {
                                    User user = vkAuthorizationRequestToUserMapper
                                            .vkAuthorizationRequestToUser(vkAuthorizationRequest);

                                    user.setUsername(username);
                                    user.setPassword(bCryptPasswordEncoder.encode(HexGeneratorUtil.generateHex()));

                                    return userApi.saveUser(user)
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
                                                        UserDataRank.NEWBIE,
                                                        HexGeneratorUtil.generateHex(),
                                                        HexGeneratorUtil.generateHex()
                                                );

                                                return Mono.when(
                                                                userApi.saveUserActivity(userActivity),
                                                                userApi.saveUserData(userData),
                                                                downloadPhotoByLinkAndUserId(vkUserDto.getResponse()[0].getPhotoMax(), userId)
                                                        )
                                                        .thenReturn(new AuthorizationResponse(jwtUtil.generateAccessToken(savedUser),
                                                                jwtUtil.generateRefreshToken(savedUser)));
                                            });
                                }
                            });
                });
    }

    @Override
    public Mono<AuthorizationResponse> handleAuthorizeByGoogle(GoogleAuthorizationRequest googleAuthorizationRequest) {
        Mono<GoogleAccessTokenDto> googleAccessTokenDtoMono =
                googleApi.getGoogleAccessTokenDtoByGoogleAccessTokenRequest(
                        new GoogleAccessTokenRequest(
                                googleOAuthProperty.getClientId(),
                                googleOAuthProperty.getClientSecret(),
                                googleOAuthProperty.getRedirectUrl(),
                                URLDecoder.decode(googleAuthorizationRequest.getCode(), StandardCharsets.UTF_8),
                                "authorization_code"
                        )
                );

        Mono<GoogleCertsDto> googleCertsDtoMono =
                googleApi.getGoogleCerts();

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

                    return userApi.existsByUsername(username)
                            .flatMap(exists -> {
                                if (exists){
                                    return userApi.findUserByUsername(username)
                                            .flatMap(user -> {
                                                long userId = user.getId();

                                                UserActivity userActivity =
                                                googleAuthorizationRequestToUserActivityMapper
                                                        .googleAuthorizationRequestToUserActivity(googleAuthorizationRequest);

                                                userActivity.setUserId(userId);

                                                return userApi.saveUserActivity(userActivity)
                                                        .thenReturn(new AuthorizationResponse(jwtUtil.generateAccessToken(user),
                                                                jwtUtil.generateRefreshToken(user)));
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

                                    return userApi.saveUser(user)
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
                                                        UserDataRank.NEWBIE,
                                                        HexGeneratorUtil.generateHex(),
                                                        HexGeneratorUtil.generateHex()
                                                );

                                                return Mono.when(userApi.saveUserActivity(userActivity), userApi.saveUserData(userData),
                                                                downloadPhotoByLinkAndUserId(claims.get("picture", String.class), userId))
                                                        .thenReturn(new AuthorizationResponse(jwtUtil.generateAccessToken(savedUser),
                                                                jwtUtil.generateRefreshToken(savedUser)));
                                            });
                                }
                            });
                });
    }

    @Override
    public Mono<AuthorizationResponse> handleAuthorizeBySteam(SteamAuthorizationRequest steamAuthorizationRequest) {
        return steamApi.authenticateSteamUser(steamAuthorizationRequest)
                    .flatMap(s -> {
                        if (!s.contains("true")){
                            return Mono.error(new InvalidCredentialsException("Invalid SteamUser"));
                        }

                        return steamApi.getSteamUserBySteamid(
                                steamOAuthProperty.getClientSecret(),
                                steamAuthorizationRequest.getOpenidIdentity().split("https%3A%2F%2Fsteamcommunity.com%2Fopenid%2Fid%2F")[1]
                        );
                    })
                .flatMap(steamUserDto -> userApi.existsByUsername(steamUserDto.getResponse().getPlayers()[0].getSteamId() + "_steam")
                        .flatMap(exists -> {
                            String username = steamUserDto.getResponse().getPlayers()[0].getSteamId() + "_steam";

                            if (exists){
                                return userApi.findUserByUsername(username)
                                        .flatMap(user -> {
                                            long userId = user.getId();

                                            UserActivity userActivity =
                                                    steamAuthorizationRequestToUserActivityMapper
                                                            .steamAuthorizationRequestToUserActivity(steamAuthorizationRequest);

                                            userActivity.setUserId(userId);

                                            return userApi.saveUserActivity(userActivity)
                                                    .thenReturn(new AuthorizationResponse(jwtUtil.generateAccessToken(user),
                                                            jwtUtil.generateRefreshToken(user)));
                                        });
                            } else {
                                User user =
                                        steamAuthorizationRequestToUserMapper
                                                .steamAuthorizationRequestToUser(steamAuthorizationRequest);

                                user.setUsername(username);
                                user.setPassword(bCryptPasswordEncoder.encode(HexGeneratorUtil.generateHex()));

                                return userApi.saveUser(user)
                                        .flatMap(savedUser -> {
                                            long userId = savedUser.getId();

                                            UserActivity userActivity =
                                                    steamAuthorizationRequestToUserActivityMapper
                                                            .steamAuthorizationRequestToUserActivity(steamAuthorizationRequest);

                                            userActivity.setUserId(userId);

                                            UserData userData = new UserData(
                                                    null,
                                                    userId,
                                                    UserDataProfileType.PUBLIC,
                                                    steamUserDto.getResponse().getPlayers()[0].getPersonaName(),
                                                    0L,
                                                    UserDataRank.NEWBIE,
                                                    HexGeneratorUtil.generateHex(),
                                                    HexGeneratorUtil.generateHex()
                                            );

                                            return Mono.when(userApi.saveUserActivity(userActivity), userApi.saveUserData(userData),
                                                            downloadPhotoByLinkAndUserId(steamUserDto.getResponse().getPlayers()[0].getAvatarFull(), userId))
                                                    .thenReturn(new AuthorizationResponse(jwtUtil.generateAccessToken(savedUser),
                                                            jwtUtil.generateRefreshToken(savedUser)));

                                        });
                            }


                        }));
    }

    @Override
    public Mono<ResponseEntity<Void>> redirectVk() {
        return Mono.fromSupplier(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(OAUTH_REDIRECT_VK_URL));

            return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
        });
    }

    @Override
    public Mono<ResponseEntity<Void>> redirectGoogle() {
        return Mono.fromSupplier(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(OAUTH_REDIRECT_GOOGLE_URL));

            return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
        });
    }

    @Override
    public Mono<ResponseEntity<Void>> redirectSteam() {
        return Mono.fromSupplier(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(OAUTH_REDIRECT_STEAM_URL));

            return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
        });
    }

    private Mono<Void> downloadPhotoByLinkAndUserId(String link, long userId){
        WebClient.Builder webClient = WebClient.builder();

        return webClient
                .baseUrl(link)
                .build()
                .get()
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToMono(byte[].class)
                .flatMap(imageBytes -> {
                    String filePath = Paths.get(UPLOAD_DIR, userId + ".jpg").toString();
                    try {
                        Files.write(Paths.get(filePath), imageBytes);
                        return Mono.empty();
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                });
    }

}
