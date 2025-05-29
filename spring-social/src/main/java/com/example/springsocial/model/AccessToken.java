package com.example.springsocial.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author yangcuncao
 * @program spring-social
 * @date 2025/5/28 18:55
 * @description
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessToken {
    // {
    //  "access_token": "1/fFAGRNJru1FTz70BzhT3Zg",
    //  "expires_in": 3920,
    //  "token_type": "Bearer",
    //  "scope": "https://www.googleapis.com/auth/drive.metadata.readonly https://www.googleapis.com/auth/calendar.readonly",
    //  "refresh_token": "1//xEoDL4iW3cxlI7yDbSRFYNG01kVKM2C-259HOF2aQbI"
    //}
    @JsonProperty("access_token") // 映射 JSON 中的下划线字段名
    private String accessToken;

    @JsonProperty("expires_in") // 映射 JSON 中的下划线字段名
    private String expiresIn;

    @JsonProperty("token_type") // 映射 JSON 中的下划线字段名
    private String tokenType;

    @JsonProperty("scope") // 映射 JSON 中的下划线字段名
    private String scope;

    @JsonProperty("refresh_token") // 映射 JSON 中的下划线字段名
    private String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
