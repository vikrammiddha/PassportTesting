/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.src.Utils;

import Common.src.com.Config.AppConfig;
import Common.src.com.SFDC.PartnerSession;
import com.sforce.soap.partner.PartnerConnection;
import com.src.PassportTest;
import com.src.bean.InputFileRow;
import com.src.bean.User;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 *
 * @author Vikram
 */
public class Utils {
    
    private FileReaderWriter fileReader;
    private AppConfig appConfig = null;
    private static Logger LOGGER = Logger.getLogger(Utils.class);
    
    public Utils(AppConfig appConfig){
        fileReader = new FileReaderWriter();
        this.appConfig = appConfig;
    }
    
    public HashMap<String,String> getOrgUserDetails(String fileName){
        
        HashMap<String,String> orgUserIdMap = new HashMap<String,String>();
        Integer rowCounter = 0;
        Integer columnCounter = 0;
        Integer orgIdIndex = 0;
        Integer userIdIndex = 0;
        Integer passIndex = 0;
        String org = "";
        String user = "";
        String pass = "";
        
        ArrayList<String> fileDataList = fileReader.readExcelFileData(fileName);
        
        if(fileDataList.size() > 0){
            for(String row : fileDataList){
                for(String field : row.split("##")){
                    if(field != null && field.trim().length() > 0){
                        if(rowCounter == 0){
                            if("OrgName".equals(field)){
                                orgIdIndex = columnCounter;
                            }else if("UserName".equals(field)){
                                userIdIndex = columnCounter;
                            }else if("Password".equals(field)){
                                passIndex = columnCounter;
                            } 
                        }else{
                            if(columnCounter == orgIdIndex){
                                 org = field;
                            }else if(columnCounter == userIdIndex){
                                 user = field;
                            }else if(columnCounter == passIndex){
                                 pass = field;
                            }
                            
                        }
                    }
                    columnCounter++;
                    
                }
                if( rowCounter > 0 && pass != null && pass.trim().length() > 0  )
                    orgUserIdMap.put(org, user + "##" + pass);
                
                rowCounter++;
                columnCounter = 0;
            }
        }
        
        return orgUserIdMap;
        
    }
    
    
    public ArrayList<InputFileRow> getInputFileBean(String fileName, Boolean allowNullPasswords){
        
        HashMap<String,String> orgUserIdMap = new HashMap<String,String>();
        HashMap<String,Integer> columnIndexMap = new HashMap<String, Integer>();
        ArrayList<InputFileRow> retList = new ArrayList<InputFileRow>();
        
        Integer rowCounter = 0;
        Integer columnCounter = 0;
        Integer orgIdIndex = 0;
        Integer userIdIndex = 0;
        Integer passIndex = 0;
        String org = "";
        String user = "";
        String pass = "";
        
        ArrayList<String> fileDataList = fileReader.readExcelFileData(fileName);
        
        if(fileDataList.size() > 0){
            for(String row : fileDataList){
                InputFileRow inputRow = new InputFileRow();
                for(String field : row.split("##")){
                    if(field != null && field.trim().length() > 0){
                        if(rowCounter == 0){
                            columnIndexMap.put(field.toUpperCase(), columnCounter);
                        }else{
                            if(columnCounter == columnIndexMap.get("INDEX")){
                                 inputRow.setIndex(Integer.valueOf(field));
                            }else if(columnCounter == columnIndexMap.get("ORGNAME")){
                                 inputRow.setOrgName(field);
                            }else if(columnCounter == columnIndexMap.get("USERCODE")){
                                 inputRow.setUserCode(field);
                            }else if(columnCounter == columnIndexMap.get("USERNAME")){
                                 inputRow.setUserName(field);
                            }else if(columnCounter == columnIndexMap.get("PASSWORD")){
                                 inputRow.setPassword(field);
                            }else if(columnCounter == columnIndexMap.get("ORGTYPE")){
                                 inputRow.setOrgType(field);
                            }else if(columnCounter == columnIndexMap.get("CONNECTIONNAME")){
                                 inputRow.setConnectionName(field);
                            }
                            
                        }
                    }
                    columnCounter++;
                    
                }
                if( rowCounter > 0  )
                    retList.add(inputRow);
                
                rowCounter++;
                columnCounter = 0;
            }
        }
        
        return retList;
        
    }
    
    public ArrayList<User> getOrgReplicaUsers(PartnerConnection connection){
        
        ArrayList<User> retList = new ArrayList<User>();
        PartnerSession sfdcSession = new PartnerSession(appConfig);
        //String query = "Select Id from User";
        ArrayList<String> queryFieldList = new ArrayList<String>();
        queryFieldList.add("Id");
        queryFieldList.add("Username");
        
        String query = " FROM USER ";
        
        try{
            ArrayList<HashMap<String, String>>  users = sfdcSession.executeQuery(queryFieldList, query, connection);

            for(HashMap<String, String> hm : users){
                User u = new User();
                u.setUserId(hm.get("ID"));
                u.setUserName(hm.get("USERNAME"));
                u.setOrgId(connection.getUserInfo().getOrganizationId());
                retList.add(u);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return retList;
        
    }
    
    public void writeDataToExcel(ArrayList<InputFileRow> inputRows){
      ArrayList<String> printRows = new ArrayList<String>();
      
      // print header row
      
      String headerRow = "OrgName##UserName##Password##OrgType##UserCode##ConnectionName##OrgId##";
      
      for(int i=0 ; i< inputRows.get(0).getUserIds().size(); i++){
          headerRow += "U" + (i+1) +"Id" + "##";
      }
      
      headerRow = headerRow.substring(0, headerRow.length() - 2);
      
      printRows.add(headerRow);
      
      for(InputFileRow row : inputRows){
          
          String singlePrintRow = "";
          
          singlePrintRow =  row.getOrgName() + "##" + row.getUserName() + "##" + row.getPassword() +
                                 "##" + row.getOrgType() + "##" + row.getUserCode() + "##" + row.getConnectionName() + "##"
                                + row.getOrgId() + "##";
                    
          //System.out.println(row.getOrgName() + " :: ");
          for(String s : row.getUserIds()){
             // System.out.println(s + " | ");
             singlePrintRow += s + "##"; 
          }
          
          singlePrintRow = singlePrintRow.substring(0, singlePrintRow.length()-2);
          printRows.add(singlePrintRow);
          
      }
      
      fileReader.writeToExcel(appConfig.getOrgUsersFile(), printRows);
    }
    
    public void clearTestData(PartnerConnection connection){
        
        ArrayList<String> fieldList = new ArrayList<String>();
        PartnerSession sfdcSession = new PartnerSession(appConfig);
        fieldList.add("Id");
        String query = " FROM FeedItem ";
        ArrayList<HashMap<String, String>>  res = sfdcSession.executeQuery(fieldList, query, connection);
        
        ArrayList<String> delIdList = new ArrayList<String>();
        for(HashMap<String, String> hm : res){
            delIdList.add(hm.get("ID"));
        }
        
        if(delIdList.size() > 0)
            sfdcSession.deleteSFDCRecords(delIdList.toArray(new String[delIdList.size()]), connection);
        
        LOGGER.info("Deleted " + delIdList.size() + " Feed Items");
        
        // Recyclebin Feeditems
        
        query = " from FeedItem where isDeleted=true ";
        
        res = sfdcSession.executeQuery(fieldList, query, connection);
        
        delIdList.clear();
        
        for(HashMap<String, String> hm : res){
            delIdList.add(hm.get("ID"));
        }
        
        if(delIdList.size() > 0)
            sfdcSession.emptyRecycleBin(delIdList.toArray(new String[delIdList.size()]), connection);
        
        LOGGER.info("Deleted " + delIdList.size() + " Feed Items from Recycle Bin");
        
        // Delete Collaboration groups
        
        query = " from CollaborationGroup ";
        
        res = sfdcSession.executeQuery(fieldList, query, connection);
        
        delIdList.clear();
        
        for(HashMap<String, String> hm : res){
            delIdList.add(hm.get("ID"));
        }
        
        if(delIdList.size() > 0)
            sfdcSession.deleteSFDCRecords(delIdList.toArray(new String[delIdList.size()]), connection);
        
        LOGGER.info("Deleted " + delIdList.size() + " Collaboration Groups");
        
        // Recyclebin Colaboration groups
        
        query = " from CollaborationGroup where isDeleted=true ";
        
        res = sfdcSession.executeQuery(fieldList, query, connection);
        
        delIdList.clear();
        
        for(HashMap<String, String> hm : res){
            delIdList.add(hm.get("ID"));
        }
        
        if(delIdList.size() > 0)
            sfdcSession.emptyRecycleBin(delIdList.toArray(new String[delIdList.size()]), connection);
        
        LOGGER.info("Deleted " + delIdList.size() + " Collaboration groups from Recycle Bin");
        
        
        // Delete Mapping
        
        query = " from PassportDev2__Mapping__c where PassportDev2__Type__c!='User' ";
        
        res = sfdcSession.executeQuery(fieldList, query, connection);
        
        delIdList.clear();
        
        for(HashMap<String, String> hm : res){
            delIdList.add(hm.get("ID"));
        }
        
        if(delIdList.size() > 0)
            sfdcSession.deleteSFDCRecords(delIdList.toArray(new String[delIdList.size()]), connection);
        
        LOGGER.info("Deleted " + delIdList.size() + " Mappings");
        
        // Recyclebin Mapping
        
        query = " from PassportDev2__Mapping__c where isDeleted=true ";
        
        res = sfdcSession.executeQuery(fieldList, query, connection);
        
        delIdList.clear();
        
        for(HashMap<String, String> hm : res){
            delIdList.add(hm.get("ID"));
        }
        
        if(delIdList.size() > 0)
            sfdcSession.emptyRecycleBin(delIdList.toArray(new String[delIdList.size()]), connection);
        
        LOGGER.info("Deleted " + delIdList.size() + " Mappings from Recycle Bin");
        
        // Delete Queue
        
        query = " from PassportDev2__Queue__c ";
        
        res = sfdcSession.executeQuery(fieldList, query, connection);
        
        delIdList.clear();
        
        for(HashMap<String, String> hm : res){
            delIdList.add(hm.get("ID"));
        }
        
        if(delIdList.size() > 0)
            sfdcSession.deleteSFDCRecords(delIdList.toArray(new String[delIdList.size()]), connection);
        
        LOGGER.info("Deleted " + delIdList.size() + " Queues");
        
        // Recyclebin Queue 
        
        query = " from PassportDev2__Queue__c where isDeleted=true ";
        
        res = sfdcSession.executeQuery(fieldList, query, connection);
        
        delIdList.clear();
        
        for(HashMap<String, String> hm : res){
            delIdList.add(hm.get("ID"));
        }
        
        if(delIdList.size() > 0)
            sfdcSession.emptyRecycleBin(delIdList.toArray(new String[delIdList.size()]), connection);
        
        LOGGER.info("Deleted " + delIdList.size() + " Queues from Recycle Bin");
        
        // Delete Logs
        
        query = " from PassportDev2__Log__c ";
        
        res = sfdcSession.executeQuery(fieldList, query, connection);
        
        delIdList.clear();
        
        for(HashMap<String, String> hm : res){
            delIdList.add(hm.get("ID"));
        }
        
        if(delIdList.size() > 0)
            sfdcSession.deleteSFDCRecords(delIdList.toArray(new String[delIdList.size()]), connection);
        
        LOGGER.info("Deleted " + delIdList.size() + " Logs");
        
        // Recyclebin Logs
        
        query = " from PassportDev2__Log__c where isDeleted=true ";
        
        res = sfdcSession.executeQuery(fieldList, query, connection);
        
        delIdList.clear();
        
        for(HashMap<String, String> hm : res){
            delIdList.add(hm.get("ID"));
        }
        
        if(delIdList.size() > 0)
            sfdcSession.emptyRecycleBin(delIdList.toArray(new String[delIdList.size()]), connection);
        
        LOGGER.info("Deleted " + delIdList.size() + " Logs from Recycle Bin");
        
        
    }
    
}
