package org.sdhub.client.porters.database;

import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.sdhub.client.porters.Activator;
import org.sdhub.client.porters.preferences.PreferenceConstants;

public class MariaDBManager {
	
	public static BasicDataSource mariaDS;
	
    public static void init() {
    	
    	IPreferenceStore store = Activator.getDefault().getPreferenceStore();
    	
    	if(!store.contains(PreferenceConstants.SD_DB_URL))
    	{
    		return;
    	}
    	
    	if(!store.contains(PreferenceConstants.SD_DB_USER))
    	{
    		return;
    	}
    	
    	if(!store.contains(PreferenceConstants.SD_DB_PASSWD))
    	{
    		return;
    	}
    	
    	String url = store.getString(PreferenceConstants.SD_DB_URL);
    	String username = store.getString(PreferenceConstants.SD_DB_USER);
    	String password = store.getString(PreferenceConstants.SD_DB_PASSWD);
    	
        try {    

			if(null == mariaDS)
			{
				mariaDS = new BasicDataSource();
				mariaDS.setUrl(url);
				mariaDS.setDriverClassName("com.mysql.jdbc.Driver");
				mariaDS.setUsername(username);
				mariaDS.setPassword(password);
		        return;
	    	}
			
			if(mariaDS.isClosed())
			{
				mariaDS = new BasicDataSource();
				mariaDS.setUrl(url);
				mariaDS.setDriverClassName("com.mysql.jdbc.Driver");
				mariaDS.setUsername(username);
				mariaDS.setPassword(password);
		        return;
			}
        } catch (Exception e) {    
            e.printStackTrace();  
        }   
    }
    
    public static void printInnerDSStats() {
    	
    	if(mariaDS != null)
    	{
    		if(!mariaDS.isClosed())
    		{
		        System.out.println("NumActive: " + mariaDS.getNumActive());
		        System.out.println("NumIdle: " + mariaDS.getNumIdle());
    		}else
    		{
    			System.out.println("innerDS has been CLOSED!");
    		}
    	}else{
    		System.out.println("innerDS is NULL ! ! !");
    	}
    	
    }

    public static void shutdownInnerDS() throws SQLException {
        if(mariaDS != null)
        {
        	if(!mariaDS.isClosed())
        	{
        		mariaDS.close();
        	}
        }
    }
}
