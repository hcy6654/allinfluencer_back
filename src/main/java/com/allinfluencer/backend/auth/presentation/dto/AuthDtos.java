package com.allinfluencer.backend.auth.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record SignupRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 6) String password,
            String displayName,
            @NotBlank String role,
            InfluencerSignup influencer,
            AdvertiserSignup advertiser
    ) {}

    public record InfluencerSignup(
            @NotBlank String[] categories,
            ChannelInput[] channels,
            VerificationInput verification
    ) {}

    public record AdvertiserSignup(
            @NotBlank String companyName,
            @NotBlank String businessRegistrationNumber,
            @NotBlank String industry,
            VerificationInput verification
    ) {}

    public record ChannelInput(
            @NotBlank String platform, // INSTAGRAM|YOUTUBE|...
            @NotBlank String channelUrl,
            String channelHandle,
            Integer followers
    ) {}

    public record VerificationInput(
            String method,
            String data
    ) {}
}

