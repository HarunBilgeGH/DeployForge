package io.deployforge.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("deployforge.bootstrap")
public record BootstrapProperties(
        @NotBlank String username,
        @NotBlank @Pattern(regexp = "[\\x20-\\x7E]{16,72}") String password) {

    @Override
    public String toString() {
        return "BootstrapProperties[credentials=REDACTED]";
    }
}
