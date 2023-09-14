package com.example.userapi.api;

import com.example.userapi.dto.UserDto;
import com.example.userapi.model.CryptoData;
import com.example.userapi.model.User;
import com.example.userapi.model.UserActivity;
import com.example.userapi.model.UserData;
import com.example.userapi.repository.CryptoDataRepository;
import com.example.userapi.repository.UserActivityRepository;
import com.example.userapi.repository.UserDataRepository;
import com.example.userapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8080")
@RequiredArgsConstructor
public class UserApi {

    private final CryptoDataRepository cryptoDataRepository;
    private final UserActivityRepository userActivityRepository;
    private final UserDataRepository userDataRepository;
    private final UserRepository userRepository;

    @GetMapping("/user/findUserByUsername")
    public Mono<UserDto> findUserByUsername(@RequestParam("username") String username) {
        Mono<User> userMono = userRepository.findUserByUsername(username);

        return userMono.flatMap(user -> {
            Mono<List<CryptoData>> cryptoDataMono = cryptoDataRepository.findAllByUserId(user.getId()).collectList();
            Mono<List<UserActivity>> userActivityMono = userActivityRepository.findAllByUserId(user.getId()).collectList();
            Mono<UserData> userDataMono = userDataRepository.findByUserId(user.getId());

            return Mono.zip(cryptoDataMono, userActivityMono, userDataMono)
                    .map(tuple -> {
                        List<CryptoData> cryptoData = tuple.getT1();
                        List<UserActivity> userActivity = tuple.getT2();
                        UserData userData = tuple.getT3();

                        UserDto userDto = new UserDto();
                        userDto.setId(user.getId());
                        userDto.setUsername(user.getUsername());
                        userDto.setPassword(user.getPassword());
                        userDto.setCryptoData(cryptoData);
                        userDto.setUserActivity(userActivity);
                        userDto.setUserData(userData);
                        userDto.setIsAccountNonLocked(user.getIsAccountNonLocked());
                        userDto.setRegisteredAt(user.getRegisteredAt());

                        return userDto;
                    });
        });
    }
    @GetMapping("/user/findOriginalUserByUsername")
    public Mono<User> findOriginalUserByUsername(@RequestParam("username") String username){
        return userRepository.findUserByUsername(username);
    }

    @Transactional
    @PostMapping("/user/save")
    public Mono<User> saveUser(@RequestBody User user){
        return userRepository.save(user);
    }

    @GetMapping("/user/existsByUsername")
    public Mono<Boolean> existsUserByUsername(@RequestParam("username") String username){
        return userRepository.findUserByUsername(username)
                .map(user -> true)
                .defaultIfEmpty(false);
    }

    @Transactional
    @PostMapping("/userData/save")
    public Mono<UserData> saveUserData(@RequestBody UserData userData){
        return userDataRepository.save(userData);
    }

    @Transactional
    @PostMapping("/userActivity/save")
    public Mono<UserActivity> saveUserActivity(@RequestBody UserActivity userActivity){
        return userActivityRepository.save(userActivity);
    }

    @Transactional
    @PostMapping("/cryptoData/saveAll")
    public Flux<CryptoData> saveAllCryptoData(@RequestBody List<CryptoData> cryptoData){
        return cryptoDataRepository.saveAll(cryptoData);
    }

}
