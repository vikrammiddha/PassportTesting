package com.src;



import Common.src.com.Config.AppConfig;
import Common.src.com.Config.Configurator;
import Common.src.com.SFDC.PartnerSession;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.PartnerConnection;

import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.src.Utils.FileReaderWriter;
import com.src.Utils.Utils;
import com.src.bean.InputFileRow;
import com.src.bean.PassportOrg;
import com.src.bean.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import org.apache.log4j.Logger;


public class PassportTest {  
  
  private static AppConfig appConfig = null;
  private Utils utils = null;
  private PartnerSession pSession = null;
  private HashMap<String,PartnerConnection> sessionMap;
  private static Logger LOGGER = Logger.getLogger(PassportTest.class);
  private ArrayList<InputFileRow> distinctOrgList;
  
  public PassportTest(){
      appConfig = Configurator.getAppConfig();
      utils = new Utils(appConfig);
      pSession = new PartnerSession(appConfig);
      sessionMap = new HashMap<String,PartnerConnection>();
      distinctOrgList = new ArrayList<InputFileRow>();
  }

  public static void main(String[] args) {      
    
      PassportTest helperObj = new PassportTest();
      LOGGER.info("Loading Sessions for all the Orgs ....");
      helperObj.getSFDCSessions();
      LOGGER.info("Sessions Loaded into the memory ...");
      
      System.out.println("Enter the number and press Enter \n");
      System.out.println("1 : Fetch Replica Users \n");
      System.out.println("2 : Clear all test data from all Orgs \n");
      System.out.println("3 : Run Test Cases \n");
      Scanner in = new Scanner(System.in);
      int choice = -1;
      
      try{
          choice = in.nextInt();
      }catch(Exception e){
          LOGGER.info("Invalid Input ...");
          System.exit(1);
      }
      
      
      switch(choice){
          case 1: LOGGER.info("Starting Fetch Replica users process ...");
                  helperObj.populateReplicaUsers();  
                  break;
          case 2: LOGGER.info("Clearing all the data ...");
                  helperObj.clearTestData();
                  break;
          default: LOGGER.info("Invalid input ...");
                  break;  
      }      
    
  }
  
  private void clearTestData(){
      
      for(InputFileRow row : distinctOrgList){
          
          LOGGER.info("Clearing data for Org :: " + row.getOrgName() + " ......");
          try{
            utils.clearTestData(sessionMap.get(row.getOrgName()));
            LOGGER.info("Cleared data for Org :: " + row.getOrgName() + " ......");
          }catch(Exception e){
              LOGGER.error(" Could not clear the data  for Org :: " + row.getOrgName() + ". Cause :  " + e.getMessage());
              e.printStackTrace();
          }
          
      }
      
  }
  
  
  private void populateReplicaUsers(){
      
      ArrayList<InputFileRow> inputRows = utils.getInputFileBean(appConfig.getOrgUsersFile(), Boolean.TRUE);
      ArrayList<PassportOrg> orgsList = new ArrayList<PassportOrg>();
      
      for(InputFileRow row : distinctOrgList){
          LOGGER.info("Fetching Replica users from Org :" + row.getOrgName());
          ArrayList<User> replicaUsers = utils.getOrgReplicaUsers(sessionMap.get(row.getOrgName()));
          
          PassportOrg po = new PassportOrg();          
          po.setOrgName(row.getOrgName());
          HashMap<String,String> replicaUsersMap = new HashMap<String,String>();
          
          for(User u : replicaUsers){
              po.setOrgId(u.getOrgId());
              replicaUsersMap.put(u.getUserName(), u.getUserId());
          }
          po.setReplicaUsersMap(replicaUsersMap);
          orgsList.add(po);
          
      }
      ArrayList<String> replicaUsersList = new ArrayList<String>();
      for(InputFileRow row : inputRows){
          
          
          String liveOrgId = "";
          String liveUserId = "";
          Boolean isOrgProcessed = false;
          
          for(PassportOrg po : orgsList){
              
              if(row.getOrgName().equals(po.getOrgName()) ){
                  isOrgProcessed = true;
                  liveOrgId = po.getOrgId();
                  replicaUsersList.add(po.getReplicaUserId(row.getUserName()));
                  liveUserId = po.getReplicaUserId(row.getUserName());
                  // No need to get replica users from the same org.
                  continue;
              }else{
                  if(isOrgProcessed == false){
                      for(PassportOrg po1 : orgsList){
                          if(row.getOrgName().equals(po1.getOrgName())){
                                isOrgProcessed = true;
                                liveOrgId = po1.getOrgId();
                                //replicaUsersList.add(po1.getReplicaUserId(row.getUserName()));
                                liveUserId = po1.getReplicaUserId(row.getUserName());
                                
                            }
                      }
                  }
                  String replicaUserIdString = liveUserId.toLowerCase() + "." + liveOrgId.toLowerCase() + "@" + po.getOrgId().toLowerCase() + ".dup";
                  replicaUsersList.add(po.getReplicaUserId(replicaUserIdString));
              }
          }
          row.setOrgId(liveOrgId);
          //row.setUserIds(replicaUsersList);
      }
      
      int counter = 0;
      for(InputFileRow row : inputRows){
          ArrayList<String> tempReplicaUsersList = new ArrayList<String>();
          for(int i = counter; i< replicaUsersList.size() ; i = i+orgsList.size()){
              tempReplicaUsersList.add(replicaUsersList.get(i));
          }
          counter ++;
          
          row.setUserIds(tempReplicaUsersList);
      }
      
      utils.writeDataToExcel(inputRows);
  }
  
  private void getSFDCSessions(){
      
      distinctOrgList = utils.getInputFileBean(appConfig.getOrgUsersFile(), Boolean.FALSE);
      sessionMap = pSession.getPartnerConnections(distinctOrgList);
      
      for(InputFileRow row : distinctOrgList){
          LOGGER.info(row.getOrgName() + " :: " + sessionMap.get(row.getOrgName()).getSessionHeader().getSessionId());
      }
  }
  
  // queries and displays the 5 newest contacts
  /*private static void queryContacts() {
    
    System.out.println("Querying for the 5 newest Contacts...");
    
    try {
       
      // query for the 5 newest contacts      
      QueryResult queryResults = connection.query("SELECT Id, FirstName, LastName, Account.Name " +
      		"FROM Contact WHERE AccountId != NULL ORDER BY CreatedDate DESC LIMIT 5");
      if (queryResults.getSize() > 0) {
    	  for (SObject s: queryResults.getRecords()) {
    	    System.out.println("Id: " + s.getId() + " " + s.getField("FirstName") + " " + 
    	        s.getField("LastName") + " - " + s.getChild("Account").getField("Name"));
    	  }
    	}
      
    } catch (Exception e) {
      e.printStackTrace();
    }    
    
  }
  
  // create 5 test Accounts
  private static void createAccounts() {
    
    System.out.println("Creating 5 new test Accounts...");
    SObject[] records = new SObject[5];

    try {
       
      // create 5 test accounts
      for (int i=0;i<5;i++) {
        SObject so = new SObject();
        so.setType("Account");
        so.setField("Name", "Test Account "+i);
        records[i] = so;
      }

      
      // create the records in Salesforce.com
      SaveResult[] saveResults = connection.create(records);
      
      // check the returned results for any errors
      for (int i=0; i< saveResults.length; i++) {
        if (saveResults[i].isSuccess()) {
          System.out.println(i+". Successfully created record - Id: " + saveResults[i].getId());
        } else {
          Error[] errors = saveResults[i].getErrors();
          for (int j=0; j< errors.length; j++) {
            System.out.println("ERROR creating record: " + errors[j].getMessage());
          }
        }    
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }    
    
  }
  
  // updates the 5 newly created Accounts
  private static void updateAccounts() {
    
    System.out.println("Update the 5 new test Accounts...");
    SObject[] records = new SObject[5];
    
    try {
       
      QueryResult queryResults = connection.query("SELECT Id, Name FROM Account ORDER BY " +
      		"CreatedDate DESC LIMIT 5");
      if (queryResults.getSize() > 0) {
    	  for (int i=0;i<queryResults.getRecords().length;i++) {
    	    SObject so = (SObject)queryResults.getRecords()[i];
    	    System.out.println("Updating Id: " + so.getId() + " - Name: "+so.getField("Name"));
    	    // create an sobject and only send fields to update
    	    SObject soUpdate = new SObject();
    	    soUpdate.setType("Account");
    	    soUpdate.setId(so.getId());
    	    soUpdate.setField("Name", so.getField("Name")+" -- UPDATED");
    	    records[i] = soUpdate;
    	  }
    	}

      
      // update the records in Salesforce.com
      SaveResult[] saveResults = connection.update(records);
      
      // check the returned results for any errors
      for (int i=0; i< saveResults.length; i++) {
        if (saveResults[i].isSuccess()) {
          System.out.println(i+". Successfully updated record - Id: " + saveResults[i].getId());
        } else {
          Error[] errors = saveResults[i].getErrors();
          for (int j=0; j< errors.length; j++) {
            System.out.println("ERROR updating record: " + errors[j].getMessage());
          }
        }    
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }    
    
  }
  
  // delete the 5 newly created Account
  private static void deleteAccounts() {
    
    System.out.println("Deleting the 5 new test Accounts...");
    String[] ids = new String[5];
    
    try {
       
      QueryResult queryResults = connection.query("SELECT Id, Name FROM Account ORDER BY " +
      		"CreatedDate DESC LIMIT 5");
      if (queryResults.getSize() > 0) {
    	  for (int i=0;i<queryResults.getRecords().length;i++) {
    	    SObject so = (SObject)queryResults.getRecords()[i];
    	    ids[i] = so.getId();
    	    System.out.println("Deleting Id: " + so.getId() + " - Name: "+so.getField("Name"));
    	  }
    	}

      
      // delete the records in Salesforce.com by passing an array of Ids
      DeleteResult[] deleteResults = connection.delete(ids);
      
      // check the results for any errors
      for (int i=0; i< deleteResults.length; i++) {
        if (deleteResults[i].isSuccess()) {
          System.out.println(i+". Successfully deleted record - Id: " + deleteResults[i].getId());
        } else {
          Error[] errors = deleteResults[i].getErrors();
          for (int j=0; j< errors.length; j++) {
            System.out.println("ERROR deleting record: " + errors[j].getMessage());
          }
        }    
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }    
    
  }*/
 
}
