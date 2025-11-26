package com.example.file.model.dto;

public record AccessLinkResult(
        boolean success,
        boolean isExpired,
        boolean isInvalidPassword,
        String redirectUrl
) {
    public static AccessLinkResult success(String url) {
        return new AccessLinkResult(true, false, false, url);
    }
    public static AccessLinkResult expired() {
        return new AccessLinkResult(false, true, false, null);
    }
    public static AccessLinkResult invalidPassword() {
        return new AccessLinkResult(false, false, true, null);
    }
}


