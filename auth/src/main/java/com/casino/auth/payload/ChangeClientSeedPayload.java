package com.casino.auth.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChangeClientSeedPayload {
    @Size(min = 0, max = 20, message = "ClientSeed cannot be less than 0 characters and more than 20")
    private String clientSeed;
}
