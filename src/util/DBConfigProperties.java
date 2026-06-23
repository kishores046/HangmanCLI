package util;



public class DBConfigProperties {
    private String DBUrl;
    private String DBUser;
    private String DBPassword;

    public String getDBPassword() {
        return DBPassword;
    }

    public void setDBUrl(String DBUrl) {
        this.DBUrl = DBUrl;
    }

    public void setDBPassword(String DBPassword) {
        this.DBPassword = DBPassword;
    }

    public void setDBUser(String DBUser) {
        this.DBUser = DBUser;
    }

    public String getDBUrl() {
        return DBUrl;
    }
    public String getDBUser() {
        return DBUser;
    }
}
