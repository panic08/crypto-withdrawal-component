package com.casino.auth.service.implement;

import com.casino.auth.api.UserApi;
import com.casino.auth.enums.UserRole;
import com.casino.auth.exception.FileSizeExceedsLimitException;
import com.casino.auth.exception.InvalidFileExtensionException;
import com.casino.auth.exception.UnauthorizedRoleException;
import com.casino.auth.payload.*;
import com.casino.auth.service.PersonalService;
import com.casino.auth.util.HexGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class PersonalServiceImpl implements PersonalService {
    private static final String UPLOAD_DIR = System.getProperty("os.name").toLowerCase().contains("linux") ?
            "/srv/photos/" : "D:/photos/";
    private final UserApi userApi;


    @Override
    public Mono<ChangeServerSeedResponse> changeServerSeed(long principalId) {
        String hex = HexGeneratorUtil.generateHex();

        return  userApi.updateServerSeedByUserId(principalId, hex).thenReturn(new ChangeServerSeedResponse(hex));
    }

    @Override
    public Mono<ChangeClientSeedPayload> changeClientSeed(long principalId, ChangeClientSeedPayload changeClientSeedPayload) {
        return userApi.updateClientSeedByUserId(principalId, changeClientSeedPayload.getClientSeed())
                        .thenReturn(new ChangeClientSeedPayload(changeClientSeedPayload.getClientSeed()));
    }

    @Override
    public Mono<ChangeProfileTypePayload> changeProfileType(long principalId, ChangeProfileTypePayload changeProfileTypePayload) {
        return userApi.updateProfileTypeByUserId(principalId, changeProfileTypePayload.getProfileType())
                        .thenReturn(new ChangeProfileTypePayload(changeProfileTypePayload.getProfileType()));
    }

    public Mono<Void> changePhoto(long principalId, FilePart multipartFile) {
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

            Path filePath = Paths.get(UPLOAD_DIR, principalId + extension);

            File file1 = new File(Paths.get(UPLOAD_DIR, principalId + ".jpg").toUri());
            File file2 = new File(Paths.get(UPLOAD_DIR, principalId + ".png").toUri());

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
    public Mono<ChangeBalancePayload> changeBalance(long principalId, ChangeBalancePayload changeBalancePayload) {
        return userApi.findUserById(principalId)
                .flatMap(user -> {
                    if (user.getRole().equals(UserRole.ADMIN)) {
                        return userApi.updateUserDataBalanceByUserId(changeBalancePayload.getBalance(), changeBalancePayload.getUserId())
                                .thenReturn(new ChangeBalancePayload(changeBalancePayload.getUserId(), changeBalancePayload.getBalance()));
                    } else {
                        return Mono.error(new UnauthorizedRoleException("You do not have enough rights"));
                    }
                });
    }

    @Override
    public Mono<ChangeIsAccountNonLockedPayload> changeIsAccountNonLocked(long principalId, ChangeIsAccountNonLockedPayload changeIsAccountNonLockedPayload) {
        return userApi.findUserById(principalId)
                .flatMap(user -> {
                    if (user.getRole().equals(UserRole.ADMIN)){
                        return userApi.updateUserAccountNonLockedById(changeIsAccountNonLockedPayload.isAccountNonLocked(), changeIsAccountNonLockedPayload.getUserId())
                                .thenReturn(new ChangeIsAccountNonLockedPayload(changeIsAccountNonLockedPayload.getUserId(), changeIsAccountNonLockedPayload.isAccountNonLocked()));
                    } else {
                        return Mono.error(new UnauthorizedRoleException("You do not have enough rights"));
                    }
                });
    }

}
