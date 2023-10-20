package com.casino.auth.service.implement;

import com.casino.auth.enums.UserDataProfileType;
import com.casino.auth.enums.UserRole;
import com.casino.auth.exception.FileSizeExceedsLimitException;
import com.casino.auth.exception.IncorrectTokenProvidedException;
import com.casino.auth.exception.InvalidFileExtensionException;
import com.casino.auth.exception.UnauthorizedRoleException;
import com.casino.auth.model.User;
import com.casino.auth.payload.*;
import com.casino.auth.property.ServicesIpProperty;
import com.casino.auth.security.jwt.JwtUtil;
import com.casino.auth.service.PersonalService;
import com.casino.auth.util.HexGeneratorUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class PersonalServiceImpl implements PersonalService {

    private final WebClient.Builder webClient;
    private final ServicesIpProperty servicesIpProperty;
    private static final String UPLOAD_DIR = System.getProperty("os.name").toLowerCase().contains("linux") ?
            "/srv/photos/" : "D:/photos/";
    private static String UPDATE_SERVER_SEED_BY_USERID;
    private static String UPDATE_CLIENT_SEED_BY_USERID;
    private static String UPDATE_PROFILE_TYPE_BY_USERID;
    private static String UPDATE_USER_ACCOUNT_NON_LOCKED_BY_ID;
    private static String UPDATE_USERDATA_BALANCE_BY_USERID;
    private static String FIND_USER_BY_ID_URL;

    @PostConstruct
    public void init() {
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
        FIND_USER_BY_ID_URL = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/user/findUserById";
    }

    @Override
    public Mono<ChangeServerSeedResponse> changeServerSeed(long id) {
        String hex = HexGeneratorUtil.generateHex();

        return  updateServerSeedByUserId(id, hex).thenReturn(new ChangeServerSeedResponse(hex));
    }

    @Override
    public Mono<ChangeClientSeedPayload> changeClientSeed(long id, ChangeClientSeedPayload changeClientSeedPayload) {
        return updateClientSeedByUserId(id, changeClientSeedPayload.getClientSeed())
                        .thenReturn(new ChangeClientSeedPayload(changeClientSeedPayload.getClientSeed()));
    }

    @Override
    public Mono<ChangeProfileTypePayload> changeProfileType(long id, ChangeProfileTypePayload changeProfileTypePayload) {
        return updateProfileTypeByUserId(id, changeProfileTypePayload.getProfileType())
                        .thenReturn(new ChangeProfileTypePayload(changeProfileTypePayload.getProfileType()));
    }

    public Mono<Void> changePhoto(long id, FilePart multipartFile) {
            long maxSizeBytes = 1024 * 1024;
            long size = multipartFile.headers().getContentLength();
            String filename = multipartFile.filename().toLowerCase();
            String extension = filename.substring(filename.lastIndexOf('.'));


            if (size > maxSizeBytes) {
                return Mono.error(new FileSizeExceedsLimitException("File size exceeds the maximum allowed size"));
            }

            if (!multipartFile.filename().toLowerCase().endsWith(".jpg") && !multipartFile.filename().toLowerCase().endsWith(".png")) {
                return Mono.error(new InvalidFileExtensionException("Invalid file extension"));
            }

            Path filePath = Paths.get(UPLOAD_DIR, id + extension);

            File file1 = new File(Paths.get(UPLOAD_DIR, id + ".jpg").toUri());
            File file2 = new File(Paths.get(UPLOAD_DIR, id + ".png").toUri());

            if (file1.exists()) {
                file1.delete();
            } else if (file2.exists()) {
                file2.delete();
            }

            return multipartFile
                    .transferTo(filePath.toFile())
                    .then(Mono.empty());
        }

    @Override
    public Mono<ChangeBalancePayload> changeBalance(long id, ChangeBalancePayload changeBalancePayload) {
        return findUserById(id)
                .flatMap(user -> {
                    if (user.getRole().equals(UserRole.ADMIN)) {
                        return updateUserDataBalanceByUserId(changeBalancePayload.getBalance(), changeBalancePayload.getUserId())
                                .thenReturn(new ChangeBalancePayload(changeBalancePayload.getUserId(), changeBalancePayload.getBalance()));
                    } else {
                        return Mono.error(new UnauthorizedRoleException("You do not have enough rights"));
                    }
                });
    }

    @Override
    public Mono<ChangeIsAccountNonLockedPayload> changeIsAccountNonLocked(long id, ChangeIsAccountNonLockedPayload changeIsAccountNonLockedPayload) {
        return findUserById(id)
                .flatMap(user -> {
                    if (user.getRole().equals(UserRole.ADMIN)){
                        return updateUserAccountNonLockedById(changeIsAccountNonLockedPayload.isAccountNonLocked(), changeIsAccountNonLockedPayload.getUserId())
                                .thenReturn(new ChangeIsAccountNonLockedPayload(changeIsAccountNonLockedPayload.getUserId(), changeIsAccountNonLockedPayload.isAccountNonLocked()));
                    } else {
                        return Mono.error(new UnauthorizedRoleException("You do not have enough rights"));
                    }
                });
    }


    private Mono<Void> updateServerSeedByUserId(long userId, String serverSeed){
        return webClient.baseUrl(UPDATE_SERVER_SEED_BY_USERID + "?userId=" + userId
                + "&serverSeed=" + serverSeed)
                .build()
                .put()
                .retrieve()
                .bodyToMono(Void.class)
                .cache();
    }

    private Mono<Void> updateClientSeedByUserId(long userId, String clientSeed){
        return webClient.baseUrl(UPDATE_CLIENT_SEED_BY_USERID + "?userId=" + userId
                        + "&clientSeed=" + clientSeed)
                .build()
                .put()
                .retrieve()
                .bodyToMono(Void.class)
                .cache();
    }

    private Mono<Void> updateProfileTypeByUserId(long userId, UserDataProfileType profileType){
        return webClient.baseUrl(UPDATE_PROFILE_TYPE_BY_USERID + "?userId=" + userId
                + "&profileType=" + profileType)
                .build()
                .put()
                .retrieve()
                .bodyToMono(Void.class)
                .cache();
    }

    private Mono<Void> updateUserDataBalanceByUserId(long balance, long userId){
        return webClient.baseUrl(UPDATE_USERDATA_BALANCE_BY_USERID + "?userId=" + userId
                        + "&balance=" + balance)
                .build()
                .put()
                .retrieve()
                .bodyToMono(Void.class)
                .cache();
    }
    private Mono<Void> updateUserAccountNonLockedById(boolean isAccountNonLocked, long id){
        return webClient.baseUrl(UPDATE_USER_ACCOUNT_NON_LOCKED_BY_ID + "?id=" + id
                        + "&accountNonLocked=" + isAccountNonLocked)
                .build()
                .put()
                .retrieve()
                .bodyToMono(Void.class)
                .cache();
    }

    private Mono<User> findUserById(long id){
        return webClient
                .baseUrl(FIND_USER_BY_ID_URL + "?id=" + id)
                .build()
                .get()
                .retrieve()
                .bodyToMono(User.class);
    }

}
