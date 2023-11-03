package com.casino.auth.service.implement;

import com.casino.auth.api.UserApi;
import com.casino.auth.dto.PublicUserCombinedDto;
import com.casino.auth.enums.UserDataProfileType;
import com.casino.auth.mapper.UserCombinedToPublicUserCombinedDtoMapperImpl;
import com.casino.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserCombinedToPublicUserCombinedDtoMapperImpl userCombinedToPublicUserCombinedDtoMapper;
    private static final String UPLOAD_DIR = System.getProperty("os.name").toLowerCase().contains("linux") ?
            "/srv/photos/" : "D:/photos/";
    private final UserApi userApi;

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
    public Mono<PublicUserCombinedDto> getUserById(long id) {
        return userApi.findUserCombinedById(id).map(userCombined -> {
            PublicUserCombinedDto publicUserCombinedDto = userCombinedToPublicUserCombinedDtoMapper.userCombinedToPublicUserCombinedDto(userCombined);

            if(publicUserCombinedDto.getUserData().getProfileType().equals(UserDataProfileType.PRIVATE)){
                publicUserCombinedDto.getUserData().setBalance(null);
                publicUserCombinedDto.getUserData().setRank(null);
            }

            return publicUserCombinedDto;
        });
    }


}
