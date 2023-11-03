package com.casino.auth.service.implement;

import com.casino.auth.api.UserApi;
import com.casino.auth.dto.UserCombinedDto;
import com.casino.auth.enums.UserDataProfileType;
import com.casino.auth.enums.UserDataRank;
import com.casino.auth.enums.UserRole;
import com.casino.auth.exception.IncorrectTokenProvidedException;
import com.casino.auth.exception.InvalidCredentialsException;
import com.casino.auth.exception.UserAlreadyExistsException;
import com.casino.auth.mapper.AuthorizationRequestToUserActivityMapperImpl;
import com.casino.auth.mapper.AuthorizationRequestToUserMapperImpl;
import com.casino.auth.mapper.UserCombinedToUserCombinedDtoMapperImpl;
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
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthorizationRequestToUserMapperImpl authorizationRequestToUserMapper;
    private final AuthorizationRequestToUserActivityMapperImpl authorizationRequestToUserActivityMapper;
    private final UserCombinedToUserCombinedDtoMapperImpl userCombinedToUserCombinedDtoMapper;
    private final UserApi userApi;

    @Override
    public Mono<AuthorizationResponse> handleRegister(AuthorizationRequest authorizationRequest) {
        return Mono.fromCallable(() -> {
            User user = authorizationRequestToUserMapper
                    .authorizationRequestToUser(authorizationRequest);

            user.setRole(UserRole.DEFAULT);
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

            return user;
        }).flatMap(user -> userApi.existsByUsername(user.getUsername())
                .flatMap(exists-> {
                    if (exists){
                        return Mono.error(new UserAlreadyExistsException("This user already exists"));
                    }

                    return userApi.saveUser(user)
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
                                        userApi.saveUserActivity(userActivity),
                                        userApi.saveUserData(userData)
                                ).thenReturn(new AuthorizationResponse(jwtUtil.generateAccessToken(savedUser),
                                        jwtUtil.generateRefreshToken(savedUser)));
                            });
                }));
    }

    @Override
    public Mono<AuthorizationResponse> handleLogin(AuthorizationRequest authorizationRequest) {
        Mono<User> userMono = userApi.findUserByUsername(authorizationRequest.getUsername());

        return userMono.flatMap(user -> {
            if (!bCryptPasswordEncoder.matches(authorizationRequest.getPassword(), user.getPassword())){
                return Mono.error(new InvalidCredentialsException("Incorrect login or password"));
            }

            UserActivity userActivity = authorizationRequestToUserActivityMapper
                    .authorizationRequestToUserActivity(authorizationRequest);

            userActivity.setUserId(user.getId());

            Mono<UserActivity> userActivityMono = userApi.saveUserActivity(userActivity);

            return Mono.zip(userMono, userActivityMono);
        }).map(objects -> {
            String accessToken = jwtUtil.generateAccessToken(objects.getT1());
            String refreshToken = jwtUtil.generateRefreshToken(objects.getT1());

            return new AuthorizationResponse(accessToken, refreshToken);
        });
    }

    @Override
    public Mono<UserCombinedDto> getUserInfo(long principalId) {
        return userApi.findUserCombinedById(principalId).flatMap(userCombined -> Mono.just(userCombinedToUserCombinedDtoMapper.userCombinedToUserCombinedDto(userCombined)));
    }

    @Override
    public Mono<AuthorizationResponse> handleRefreshAccessToken(String authorization) {
        return Mono.fromCallable(() -> jwtUtil.extractIdFromRefreshToken(authorization.split(" ")[1]))
                .onErrorResume(ex -> Mono.error(new IncorrectTokenProvidedException("Incorrect refresh token")))
                .flatMap(userApi::findUserById)
                .map(user -> new AuthorizationResponse(jwtUtil.generateAccessToken(user), null));
    }

}
