package com.fetch.ds.entity;


import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "user_logins")
public class UserLogin {

    @Id
    @Column(name = "user_id", length = 128)
    @JsonProperty("user_id")
    private String userId;

    @Column(name = "device_type", length = 32)
    @JsonProperty("device_type")
    private String deviceType;

    @Column(name = "masked_ip", length = 256)
    @JsonProperty("ip")
    private String maskedIp;

    @Column(name = "masked_device_id", length = 256)
    @JsonProperty("device_id")
    private String maskedDeviceId;

    @Column(name = "locale", length = 32)
    private String locale;

    @Column(name = "app_version")
    private Integer appVersion;

    @Column(name = "create_date")
    private Date createDate;

    public String getUserId() {
        return userId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getMaskedIp() {
        return maskedIp;
    }

    public String getMaskedDeviceId() {
        return maskedDeviceId;
    }

    public String getLocale() {
        return locale;
    }

    public Integer getAppVersion() {
        return appVersion;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setMaskedIp(String maskedIp) {
        this.maskedIp = maskedIp;
    }

    public void setMaskedDeviceId(String maskedDeviceId) {
        this.maskedDeviceId = maskedDeviceId;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

//    public void setAppVersion(String appVersion) {
//        this.appVersion = appVersion;
//    }

    @JsonProperty("app_version")
    public void setAppVersionAsString(String appVersion) {
        this.appVersion = appVersion != null ? Integer.parseInt(appVersion.replace(".", "")) : null;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
