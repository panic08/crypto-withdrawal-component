package com.casino.auth.service.implement;

import com.casino.auth.exception.FileSizeExceedsLimitException;
import com.casino.auth.exception.InvalidCredentialsException;
import com.casino.auth.exception.InvalidFileExtensionException;
import com.casino.auth.model.User;
import com.casino.auth.payload.ChangeClientSeedRequest;
import com.casino.auth.payload.ChangeClientSeedResponse;
import com.casino.auth.payload.ChangeServerSeedResponse;
import com.casino.auth.security.jwt.JwtUtil;
import com.casino.auth.service.PersonalService;
import com.casino.auth.util.HexGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PersonalServiceImpl implements PersonalService {

    private final WebClient.Builder webClient;
    private final JwtUtil jwtUtil;
    private static final String UPLOAD_DIR = System.getProperty("os.name").toLowerCase().contains("linux") ?
            "/srv/photos/" : "D:/photos/";
    private static final String FIND_ORIGINAL_USER_BY_USERNAME_URL = "http://localhost:8081/api/user/findOriginalUserByUsername";
    private static final String UPDATE_SERVER_SEED_BY_USERID = "http://localhost:8081/api/userData/updateServerSeedByUserId";
    private static final String UPDATE_CLIENT_SEED_BY_USERID = "http://localhost:8081/api/userData/updateClientSeedByUserId";

    @Override
    public Mono<ChangeServerSeedResponse> changeServerSeed(String authorization) {
        return jwtUtil.extractUsername(authorization.split(" ")[1])
                .flatMap(this::findOriginalUserByUsername)
                .onErrorResume(ex -> Mono.error(new InvalidCredentialsException("Incorrect token")))
                .flatMap(user -> {
                    String hex = HexGeneratorUtil.generateHex();

                    return changeServerSeed(user.getId(), hex).thenReturn(new ChangeServerSeedResponse(hex));
                });
    }

    @Override
    public Mono<ChangeClientSeedResponse> changeClientSeed(String authorization, ChangeClientSeedRequest changeClientSeedRequest) {
        return jwtUtil.extractUsername(authorization.split(" ")[1])
                .flatMap(this::findOriginalUserByUsername)
                .onErrorResume(ex -> Mono.error(new InvalidCredentialsException("Incorrect token")))
                .flatMap(user -> changeClientSeed(user.getId(),
                        changeClientSeedRequest.getClientSeed()).thenReturn(new ChangeClientSeedResponse(changeClientSeedRequest.getClientSeed())));
    }

    @Override
    public Mono<ResponseEntity<Resource>> getPhoto(long id) {
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

    public Mono<Void> uploadPhoto(String authorization, FilePart multipartFile) {
        return jwtUtil.extractUsername(authorization.split(" ")[1])
                .flatMap(this::findOriginalUserByUsername)
                .onErrorResume(ex -> Mono.error(new InvalidCredentialsException("Incorrect token")))
                .flatMap(user -> {
                    long id = user.getId();

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
                    File file = new File(filePath.toUri());

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
                });
    }


    private Mono<User> findOriginalUserByUsername(String username){
        return webClient
                .baseUrl(FIND_ORIGINAL_USER_BY_USERNAME_URL + "?username=" + username)
                .build()
                .get()
                .retrieve()
                .bodyToMono(User.class);
    }
    private Mono<Void> changeServerSeed(long userId, String serverSeed){
        return webClient.baseUrl(UPDATE_SERVER_SEED_BY_USERID + "?user_id=" + userId
                + "&server_seed=" + serverSeed)
                .build()
                .put()
                .retrieve()
                .bodyToMono(Void.class)
                .cache();
    }

    private Mono<Void> changeClientSeed(long userId, String clientSeed){
        return webClient.baseUrl(UPDATE_CLIENT_SEED_BY_USERID + "?user_id=" + userId
                        + "&client_seed=" + clientSeed)
                .build()
                .put()
                .retrieve()
                .bodyToMono(Void.class)
                .cache();
    }

}
