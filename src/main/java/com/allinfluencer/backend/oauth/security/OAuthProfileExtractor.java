package com.allinfluencer.backend.oauth.security;

import com.allinfluencer.backend.oauth.application.OAuthIntegrationService.OAuthProfile;

import java.util.Map;

public final class OAuthProfileExtractor {
    private OAuthProfileExtractor() {}

    public static OAuthProfile extract(String registrationId, Map<String, Object> attributes) {
        String id = null;
        String email = null;
        String name = null;
        String avatar = null;

        switch (registrationId.toLowerCase()) {
            case "google" -> {
                id = str(attributes.get("sub"));
                email = str(attributes.get("email"));
                name = str(attributes.get("name"));
                avatar = str(attributes.get("picture"));
            }
            case "kakao" -> {
                id = str(attributes.get("id"));
                Map<String, Object> account = map(attributes.get("kakao_account"));
                Map<String, Object> props = map(attributes.get("properties"));
                if (account != null) email = str(account.get("email"));
                if (props != null) {
                    name = str(props.get("nickname"));
                    avatar = str(props.get("profile_image"));
                }
            }
            case "naver" -> {
                Map<String, Object> resp = map(attributes.get("response"));
                if (resp != null) {
                    id = str(resp.get("id"));
                    email = str(resp.get("email"));
                    name = str(resp.get("name"));
                    avatar = str(resp.get("profile_image"));
                }
            }
            default -> {
                // best-effort
                id = str(attributes.get("id"));
                email = str(attributes.get("email"));
                name = str(attributes.get("name"));
            }
        }

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("no_user_data");
        }

        return new OAuthProfile(id, email, name, avatar);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object o) {
        if (o instanceof Map<?, ?> m) {
            return (Map<String, Object>) m;
        }
        return null;
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}

