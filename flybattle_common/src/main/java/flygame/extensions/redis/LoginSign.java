package flygame.extensions.redis;

import java.io.Serializable;


public class LoginSign implements Serializable {

    private String sessionId;
    private String accountId;
    private int platformId;
    private int zoneId;

    public LoginSign() {}

    public LoginSign(String sessionId, String accountId, int platformId, int zoneId) {
        this.sessionId = sessionId;
        this.accountId = accountId;
        this.platformId = platformId;
        this.zoneId = zoneId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public int getPlatformId() {
        return platformId;
    }

    public void setPlatformId(int platformId) {
        this.platformId = platformId;
    }

    public int getZoneId() {
        return zoneId;
    }

    public void setZoneId(int zoneId) {
        this.zoneId = zoneId;
    }

}
