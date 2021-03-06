package Common.src.com.Config;

/* 
########################################################################### 
# File..................: AppConfig.java
# Version...............: 1.0
# Created by............: Vikram Middha
# Created Date..........: 27-Jul-2012
# Last Modified by......: 
# Last Modified Date....: 
# Description...........: This is a bean class for Resilient.properties file.
*                        For each entry in properties file, there should be a 
*                       field in this class and getter/setter for the same.
# Change Request History: 				   							 
########################################################################### 
*/

public final class AppConfig {
	
    // SFDC
    private String sfdcEndpoint;
    private String sfdcUsername;
    private String sfdcPassword;
    private String orgUsersFile;
    private Boolean clearDataByDefault;

    public Boolean getClearDataByDefault() {
        return clearDataByDefault;
    }

    public void setClearDataByDefault(Boolean clearDataByDefault) {
        this.clearDataByDefault = clearDataByDefault;
    }

    public String getOrgUsersFile() {
        return orgUsersFile;
    }

    public void setOrgUsersFile(String orgUsersFile) {
        this.orgUsersFile = orgUsersFile;
    }  
    
    public String getSfdcEndpoint() {
        return sfdcEndpoint;
    }
    public void setSfdcEndpoint(String sfdcEndpoint) {
        this.sfdcEndpoint = sfdcEndpoint;
    }
    
    public String getSfdcUsername() {
        return sfdcUsername;
    }
    public void setSfdcUsername(String sfdcUsername) {
        this.sfdcUsername = sfdcUsername;
    }
    
    public String getSfdcPassword() {
        return sfdcPassword;
    }
    public void setSfdcPassword(String sfdcPassword) {
        this.sfdcPassword = sfdcPassword;
    }
   
    
}