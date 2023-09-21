package com.casino.auth.service.implement;

import com.casino.auth.dto.PublishUserDto;
import com.casino.auth.dto.UserDto;
import com.casino.auth.enums.UserDataProfileType;
import com.casino.auth.service.UserService;
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
    private static final String UPLOAD_DIR = System.getProperty("os.name").toLowerCase().contains("linux") ?
            "/srv/photos/" : "D:/photos/";

    private static final String FIND_USER_BY_ID_URL = "http://localhost:8081/api/user/findUserById";

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
}
