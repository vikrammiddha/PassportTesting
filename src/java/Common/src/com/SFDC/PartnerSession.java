/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Common.src.com.SFDC;

import Common.src.com.Config.AppConfig;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.src.bean.InputFileRow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vikram
 */
public class PartnerSession {
    
    private AppConfig appConfig;
    
    public PartnerSession(AppConfig appConfig){
        
        this.appConfig = appConfig;
        
    }
    
    public HashMap<String,PartnerConnection> getPartnerConnections(ArrayList<InputFileRow> inputRowList){
        
        HashMap<String,PartnerConnection> retMap = new HashMap<String, PartnerConnection>();
        
        for(InputFileRow row : inputRowList){
            ConnectorConfig config = new ConnectorConfig();
            config.setUsername(row.getUserName());
            config.setPassword(row.getPassword());
        
        
            try {
                PartnerConnection connection;
                connection = Connector.newConnection(config);
                retMap.put(row.getOrgName(), connection);
                //System.out.println(org + " :: SessionId " + config.getSessionId());

            // run the different examples
           // queryContacts();
            //createAccounts();
            //updateAccounts();
            //deleteAccounts();


            } catch (ConnectionException e1) {
                e1.printStackTrace();
            } 
        }
        
        return retMap;
    }
    
    public  ArrayList<HashMap<String, String>> executeQuery(ArrayList<String> fields,String clause, PartnerConnection connection) {
        
        ArrayList<HashMap<String, String>> retMap = new ArrayList<HashMap<String, String>>();
        
        connection.setQueryOptions(250);
        
        try {
            String query = "SELECT ";
            Integer counter = 1;
            
            for(String field : fields){
                if(counter < fields.size())
                    query += field + " , ";
                else
                    query += field + " ";
                counter++;
            }
            
            query += clause;
            
          boolean done = false;  
          // query for the 5 newest contacts      
          QueryResult queryResults = connection.query(query);
          
          while (!done) {
              
            if (queryResults.getSize() > 0) {
              for (SObject s: queryResults.getRecords()) {
                  
                  HashMap<String,String> tempMap = new HashMap<String,String>();
                          
                  for(String field : fields){
                      if(field.toUpperCase().equals("ID")){
                          tempMap.put(field.toUpperCase(), s.getId());
                      }else{
                          tempMap.put(field.toUpperCase(), s.getField(field).toString());
                      }
                      
                  }  
                  
                  retMap.add(tempMap);
              }
              
              if (queryResults.isDone()) {
                    done = true;
                } else {
                    queryResults = connection.queryMore(queryResults.getQueryLocator());
                }
            }
              
          }
         

        } catch (Exception e) {
          e.printStackTrace();
        }    
        
        return retMap;
  }
    
    public void deleteSFDCRecords(String[] ids, PartnerConnection connection) {
    
        
    try {
       
      // delete the records in Salesforce.com by passing an array of Ids
      DeleteResult[] deleteResults = connection.delete(ids);
      
      // check the results for any errors
      for (int i=0; i< deleteResults.length; i++) {
        if (deleteResults[i].isSuccess()) {
          //System.out.println(i+". Successfully deleted record - Id: " + deleteResults[i].getId());
        } else {
            com.sforce.soap.partner.Error[] errors = deleteResults[i].getErrors();
          for (int j=0; j< errors.length; j++) {
            System.out.println("ERROR deleting record: " + errors[j].getMessage());
          }
        }    
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    } 
   
    
  }
     
    public void emptyRecycleBin(String[] ids, PartnerConnection connection){
        try {
            connection.emptyRecycleBin(ids);
        } catch (ConnectionException ex) {
            Logger.getLogger(PartnerSession.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
