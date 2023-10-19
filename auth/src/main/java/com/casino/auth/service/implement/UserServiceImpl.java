package com.casino.auth.service.implement;

import com.casino.auth.dto.PublishUserDto;
import com.casino.auth.enums.UserDataProfileType;
import com.casino.auth.enums.UserRole;
import com.casino.auth.exception.IncorrectTokenProvidedException;
import com.casino.auth.exception.UnauthorizedRoleException;
import com.casino.auth.model.User;
import com.casino.auth.payload.ChangeBalancePayload;
import com.casino.auth.payload.ChangeIsAccountNonLockedPayload;
import com.casino.auth.property.ServicesIpProperty;
import com.casino.auth.security.jwt.JwtUtil;
import com.casino.auth.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final WebClient.Builder webClient;
    private final ServicesIpProperty servicesIpProperty;
    private final JwtUtil jwtUtil;
    private static final String UPLOAD_DIR = System.getProperty("os.name").toLowerCase().contains("linux") ?
            "/srv/photos/" : "D:/photos/";
    private static String FIND_USER_BY_ID_URL;
    private static String FIND_ORIGINAL_USER_BY_ID_URL;
    private static String UPDATE_USERDATA_BALANCE_BY_USERID;
    private static String UPDATE_USER_ACCOUNT_NON_LOCKED_BY_ID;

    @PostConstruct
    public void init() {
        FIND_USER_BY_ID_URL = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/user/findUserById";
        UPDATE_USERDATA_BALANCE_BY_USERID = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/userData/updateBalanceByUserId";
        UPDATE_USER_ACCOUNT_NON_LOCKED_BY_ID = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/user/updateAccountNonLockedById";
        FIND_ORIGINAL_USER_BY_ID_URL = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/user/findOriginalUserById";
    }

    @Override
    public Mono<ResponseEntity<Resource>> getUserPhotoById(long id) {
        return Mono.fromSupplier(() -> {
            try {
                File file1 = new File(Paths.get(UPLOAD_DIR, id + ".jpg").toUri());
                File file2 = new File(Paths.get(UPLOAD_DIR, id + ".png").toUri());

                if (file1.exists()) {
                    Resource resource = new FileSystemResource(file1);

                    return ResponseEntity.ok()
                            .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
                            .contentLength(file1.length())
                            .contentType(MediaType.IMAGE_JPEG)
                            .body(resource);
                } else if (file2.exists()) {
                    Resource resource = new FileSystemResource(file2);

                    return ResponseEntity.ok()
                            .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
                            .contentLength(file2.length())
                            .contentType(MediaType.IMAGE_PNG)
                            .body(resource);
                } else {
                    return ResponseEntity.notFound().build();
                }
            } catch (Exception e) {
                return ResponseEntity.status(500).build();
            }
        });
    }

    @Override
    public Mono<PublishUserDto> getUserById(long id) {
        return findUserById(id).map(userDto -> {
            if(userDto.getUserData().getProfileType().equals(UserDataProfileType.PRIVATE)){
                userDto.getUserData().setBalance(null);
                userDto.getUserData().setRank(null);
            }

            return userDto;
        });
    }


    private Mono<PublishUserDto> findUserById(long id){
        return webClient
                .baseUrl(FIND_USER_BY_ID_URL + "?id=" + id)
                .build()
                .get()
                .retrieve()
                .bodyToMono(PublishUserDto.class);
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

    private Mono<User> findOriginalUserById(long id){
        return webClient
                .baseUrl(FIND_ORIGINAL_USER_BY_ID_URL + "?id=" + id)
                .build()
                .get()
                .retrieve()
                .bodyToMono(User.class);
    }
}
