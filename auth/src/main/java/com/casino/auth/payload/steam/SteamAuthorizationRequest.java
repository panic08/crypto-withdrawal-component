package com.casino.auth.payload.steam;

import com.casino.auth.payload.google.GoogleAuthorizationRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SteamAuthorizationRequest {
    @JsonProperty("openid.ns")
    private String openidNs;
    @JsonProperty("openid.mode")
    private String openidMode;
    @JsonProperty("openid.op_endpoint")
    private String openidOpEndpoint;
    @JsonProperty("openid.claimed_id")
    private String openidClaimedId;
    @JsonProperty("openid.identity")
    private String openidIdentity;
    @JsonProperty("openid.return_to")
    private String openidReturnTo;
    @JsonProperty("openid.response_nonce")
    private String openidResponseNonce;
    @JsonProperty("openid.assoc_handle")
    private String openidAssocHandle;
    @JsonProperty("openid.signed")
    private String openidSigned;
    @JsonProperty("openid.sig")
    private String openidSig;

    @NotNull(message = "Data field is required")
    private GoogleAuthorizationRequest.Data data;

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data {
        @JsonProperty("ipaddress")
        private String ipAddress;
        private String browserName;
        private String browserVersion;
        private String operatingSystem;
    }
}
