/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.src.bean;

import java.util.HashMap;

/**
 *
 * @author Vikram
 */
public class PassportOrg {
    
    String orgName;
    String orgId;
    HashMap<String,String> replicaUsersMap; 
    
    public String getReplicaUserId(String userName){
        return replicaUsersMap.get(userName);
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public HashMap<String, String> getReplicaUsersMap() {
        return replicaUsersMap;
    }

    public void setReplicaUsersMap(HashMap<String, String> replicaUsersMap) {
        this.replicaUsersMap = replicaUsersMap;
    }
   
    
}
