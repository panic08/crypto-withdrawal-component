package com.casino.auth.dto.google;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GoogleCertsDto {
    private List<Key> keys;
    @Getter
    @Setter
    public static class Key{
        private String alg;
        private String kty;
        private String e;
        private String kid;
        private String n;
        private String use;
    }
}
