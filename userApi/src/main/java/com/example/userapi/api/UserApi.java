package com.example.userapi.api;

import com.example.userapi.dto.UserDto;
import com.example.userapi.enums.UserDataProfileType;
import com.example.userapi.model.User;
import com.example.userapi.model.UserActivity;
import com.example.userapi.model.UserData;
import com.example.userapi.repository.UserActivityRepository;
import com.example.userapi.repository.UserDataRepository;
import com.example.userapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8080")
@RequiredArgsConstructor
public class UserApi {

    private final UserActivityRepository userActivityRepository;
    private final UserDataRepository userDataRepository;
    private final UserRepository userRepository;

    @GetMapping("/user/findUserByUsername")
    public Mono<UserDto> findUserByUsername(@RequestParam("username") String username) {
        Mono<User> userMono = userRepository.findUserByUsername(username);

        return userMono.flatMap(user -> {
            Mono<List<UserActivity>> userActivityMono = userActivityRepository.findAllByUserId(user.getId()).collectList();
            Mono<UserData> userDataMono = userDataRepository.findByUserId(user.getId());

            return Mono.zip(userActivityMono, userDataMono)
                    .map(tuple -> {
                        List<UserActivity> userActivity = tuple.getT1();
                        UserData userData = tuple.getT2();

                        UserDto userDto = new UserDto();
                        userDto.setId(user.getId());
                        userDto.setUsername(user.getUsername());
                        userDto.setPassword(user.getPassword());
                        userDto.setRole(user.getRole());
                        userDto.setUserActivity(userActivity);
                        userDto.setUserData(userData);
                        userDto.setIsAccountNonLocked(user.getIsAccountNonLocked());
                        userDto.setRegisteredAt(user.getRegisteredAt());

                        return userDto;
                    });
        });
    }

    @GetMapping("/user/findUserById")
    public Mono<UserDto> findUserById(@RequestParam("id") long id) {
        Mono<User> userMono = userRepository.findById(id);

        return userMono.flatMap(user -> {
            Mono<List<UserActivity>> userActivityMono = userActivityRepository.findAllByUserId(user.getId()).collectList();
            Mono<UserData> userDataMono = userDataRepository.findByUserId(user.getId());

            return Mono.zip(userActivityMono, userDataMono)
                    .map(tuple -> {
                        List<UserActivity> userActivity = tuple.getT1();
                        UserData userData = tuple.getT2();

                        UserDto userDto = new UserDto();
                        userDto.setId(user.getId());
                        userDto.setUsername(user.getUsername());
                        userDto.setPassword(user.getPassword());
                        userDto.setUserActivity(userActivity);
                        userDto.setUserData(userData);
                        userDto.setIsAccountNonLocked(user.getIsAccountNonLocked());
                        userDto.setRole(user.getRole());
                        userDto.setRegisteredAt(user.getRegisteredAt());

                        return userDto;
                    });
        });
    }

    @GetMapping("/userData/findUserDataByUserId")
    public Mono<UserData> findUserDataByUsername(@RequestParam("userId") long userId){
        return userDataRepository.findByUserId(userId);
    }

    @GetMapping("/user/findOriginalUserByUsername")
    public Mono<User> findOriginalUserByUsername(@RequestParam("username") String username){
        return userRepository.findUserByUsername(username);
    }

    @GetMapping("/user/findOriginalUserById")
    public Mono<User> findOriginalUserById(@RequestParam("id") long id){
        return userRepository.findById(id);
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

    @PutMapping("/userData/updateServerSeedByUserId")
    public Mono<Void> updateServerSeedByUsername(
            @RequestParam("userId") long userId,
            @RequestParam("serverSeed") String serverSeed
    ){
        return userDataRepository
                .updateServerSeedByUserId(serverSeed, userId);
    }

    @PutMapping("/user/updateAccountNonLockedById")
    public Mono<Void> updateAccountNonLocked(
            @RequestParam("id") long id,
            @RequestParam("accountNonLocked") boolean isAccountNonLocked
    ){
        return userRepository.updateAccountNonLockedById(isAccountNonLocked, id);
    }

    @PutMapping("/userData/updateClientSeedByUserId")
    public Mono<Void> updateClientSeedByUsername(
            @RequestParam("userId") long userId,
            @RequestParam("clientSeed") String clientSeed
    ){
        return userDataRepository
                .updateClientSeedByUserId(clientSeed, userId);
    }

    @PutMapping("/userData/updateBalanceByUserId")
    public Mono<Void> updateBalanceByUserId(
            @RequestParam("userId") long userId,
            @RequestParam("balance") long balance
    ){
        return userDataRepository.updateBalanceByUserId(balance, userId);
    }

    @PutMapping("/userData/updateProfileTypeByUserId")
    public Mono<Void> updateProfileTypeByUserId(@RequestParam("userId") long userId,
                                                  @RequestParam("profileType") UserDataProfileType profileType
    ){
        return userDataRepository.updateProfileTypeByUserId(profileType, userId);
    }


}
