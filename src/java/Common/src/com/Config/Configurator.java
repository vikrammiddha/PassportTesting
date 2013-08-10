package Common.src.com.Config;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;


/* 
########################################################################### 
# File..................: Configurator.java
# Version...............: 1.0
# Created by............: Vikram Middha
# Created Date..........: 27-Jul-2012
# Last Modified by......: 
# Last Modified Date....: 
# Description...........: This class reads the Resilient.properties file and 
*                         populates AppConfig object.
# Change Request History: 				   							 
########################################################################### 
*/
public class Configurator {

    private static Logger LOGGER = Logger.getLogger(Configurator.class);

    /**
     * Private constructor.
     */
    private Configurator() {
            throw new UnsupportedOperationException("Class is not instantiable.");
    }

    /**
     * initialize and get the Configuration
     * 
     * @return
     */
    public static AppConfig getAppConfig()  {

        LOGGER.info("Loading the configurations from properties file .........................");

        Properties props = new Properties();
        AppConfig appConfig = new AppConfig();

        try {
                File directory = new File (".");
                LOGGER.info("Canonical path ==== "+ directory.getCanonicalPath());
                
                FileInputStream fis ;
                
                try{
                    fis = new FileInputStream("C:/Temp/Passport.properties");
                }catch(FileNotFoundException e){
                    try{
                        LOGGER.info("Canonical path ====" + directory.getCanonicalPath().substring(0,directory.getCanonicalPath().lastIndexOf("\\")) + "\\webapps\\GenerateReports\\Passport.properties");
                        fis = new FileInputStream(directory.getCanonicalPath().substring(0,directory.getCanonicalPath().lastIndexOf("\\")) + "\\webapps\\GenerateReports\\Passport.properties"); 
                    }catch(Exception e1){
                        LOGGER.info("Canonical path ====" + directory.getCanonicalPath()+ "\\webapps\\GenerateReports\\Passport.properties");
                        fis = new FileInputStream(directory.getCanonicalPath()+ "\\webapps\\GenerateReports\\Passport.properties"); 
                    }
                }catch(Exception e){
                    fis = new FileInputStream("C:/Passport.properties");
                }                
                             
                props.load(fis);
               
                LOGGER.info(" Configuration Properties loaded successfully ");
                appConfig.setSfdcEndpoint(props.getProperty("sfdc.sfdcEndpoint"));
                appConfig.setSfdcUsername(props.getProperty("sfdc.sfdcUsername"));
                appConfig.setSfdcPassword(props.getProperty("sfdc.sfdcPassword"));
                appConfig.setOrgUsersFile(props.getProperty("file.orgUserIdsFilePath"));
                appConfig.setClearDataByDefault(props.getProperty("passport.clearAllDataByDefault").toUpperCase().equals("YES") ? Boolean.TRUE : Boolean.FALSE);
                


        } catch (Exception e) {
            LOGGER.error("Exception while configuring the Application credentials ..." + e);
            //throw new ResilientException(e.getMessage());
        } 
        return appConfig;
    }

}
