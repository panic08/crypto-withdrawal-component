package com.casino.auth.service.implement;

import com.casino.auth.enums.UserDataProfileType;
import com.casino.auth.exception.FileSizeExceedsLimitException;
import com.casino.auth.exception.InvalidCredentialsException;
import com.casino.auth.exception.InvalidFileExtensionException;
import com.casino.auth.payload.*;
import com.casino.auth.security.jwt.JwtUtil;
import com.casino.auth.service.PersonalService;
import com.casino.auth.util.HexGeneratorUtil;
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
    private final JwtUtil jwtUtil;
    private static final String UPLOAD_DIR = System.getProperty("os.name").toLowerCase().contains("linux") ?
            "/srv/photos/" : "D:/photos/";
    private static final String UPDATE_SERVER_SEED_BY_USERID = "http://localhost:8081/api/userData/updateServerSeedByUserId";
    private static final String UPDATE_CLIENT_SEED_BY_USERID = "http://localhost:8081/api/userData/updateClientSeedByUserId";
    private static final String UPDATE_PROFILE_TYPE_BY_USERID = "http://localhost:8081/api/userData/updateProfileTypeByUserId";

    @Override
    public Mono<ChangeServerSeedResponse> changeServerSeed(String authorization) {
        return jwtUtil.extractId(authorization.split(" ")[1])
                .onErrorResume(ex -> Mono.error(new InvalidCredentialsException("Incorrect token")))
                .flatMap(id -> {
                    String hex = HexGeneratorUtil.generateHex();

                    return changeServerSeed(id, hex).thenReturn(new ChangeServerSeedResponse(hex));
                });
    }

    @Override
    public Mono<ChangeClientSeedResponse> changeClientSeed(String authorization, ChangeClientSeedRequest changeClientSeedRequest) {
        return jwtUtil.extractId(authorization.split(" ")[1])
                .onErrorResume(ex -> Mono.error(new InvalidCredentialsException("Incorrect token")))
                .flatMap(id -> changeClientSeed(id,
                        changeClientSeedRequest.getClientSeed())
                        .thenReturn(new ChangeClientSeedResponse(changeClientSeedRequest.getClientSeed())));
    }

    @Override
    public Mono<ChangeProfileTypeResponse> changeProfileType(String authorization, ChangeProfileTypeRequest changeProfileTypeRequest) {
        return jwtUtil.extractId(authorization.split(" ")[1])
                .onErrorResume(ex -> Mono.error(new InvalidCredentialsException("Incorrect token")))
                .flatMap(id -> changeProfileType(id,
                        changeProfileTypeRequest.getProfileType())
                        .thenReturn(new ChangeProfileTypeResponse(changeProfileTypeRequest.getProfileType())));
    }

    public Mono<Void> changePhoto(String authorization, FilePart multipartFile) {
        return jwtUtil.extractId(authorization.split(" ")[1])
                .onErrorResume(ex -> Mono.error(new InvalidCredentialsException("Incorrect token")))
                .flatMap(id -> {

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
                });
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

    private Mono<Void> changeProfileType(long userId, UserDataProfileType profileType){
        return webClient.baseUrl(UPDATE_PROFILE_TYPE_BY_USERID + "?user_id=" + userId
                + "&profile_type=" + profileType)
                .build()
                .put()
                .retrieve()
                .bodyToMono(Void.class)
                .cache();
    }

}
