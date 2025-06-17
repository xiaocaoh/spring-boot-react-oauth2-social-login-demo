//package com.example.springsocial.model;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//
//// 使用 @JsonIgnoreProperties 忽略未知字段（避免 JSON 中有额外字段时解析失败）
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class UserProfile {
//    private String id;
//    private String email;
//
//    @JsonProperty("verified_email") // 映射 JSON 中的下划线字段名
//    private boolean verifiedEmail;
//
//    private String name;
//
//    @JsonProperty("given_name")
//    private String givenName;
//
//    @JsonProperty("family_name")
//    private String familyName;
//    private String picture;
//
//    // 无参构造函数（某些库需要）
//    public UserProfile() {
//    }
//
//    // Getter 和 Setter 方法
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public boolean isVerifiedEmail() {
//        return verifiedEmail;
//    }
//
//    public void setVerifiedEmail(boolean verifiedEmail) {
//        this.verifiedEmail = verifiedEmail;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getGivenName() {
//        return givenName;
//    }
//
//    public void setGivenName(String givenName) {
//        this.givenName = givenName;
//    }
//
//    public String getFamilyName() {
//        return familyName;
//    }
//
//    public void setFamilyName(String familyName) {
//        this.familyName = familyName;
//    }
//
//    public String getPicture() {
//        return picture;
//    }
//
//    public void setPicture(String picture) {
//        this.picture = picture;
//    }
//
//    // 可选：重写 toString 方法
//    @Override
//    public String toString() {
//        return "UserProfile{" +
//                "id='" + id + '\'' +
//                ", email='" + email + '\'' +
//                ", verifiedEmail=" + verifiedEmail +
//                ", name='" + name + '\'' +
//                ", givenName='" + givenName + '\'' +
//                ", familyName='" + familyName + '\'' +
//                ", picture='" + picture + '\'' +
//                '}';
//    }
//}