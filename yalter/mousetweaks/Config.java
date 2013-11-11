package yalter.mousetweaks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;

public class Config
{
    private String  fileName;
    private HashMap Properties = new HashMap();
    
    public Config( String fileName )
    {
        this.fileName = fileName;
    }
    
    public boolean readConfig()
    {
        Properties.clear();
        
        try
        {
            File config = new File( fileName );
            if ( !config.exists() )
                return false;
            
            FileReader fr = new FileReader( config );
            BufferedReader br = new BufferedReader( fr );
            String strLine = "";
            
            while ( ( strLine = br.readLine() ) != null )
            {
                String[] strKeyValue = strLine.split( "=" );
                
                int l = strKeyValue.length;
                
                if ( l < 2 )
                {
                    continue;
                }
                
                strKeyValue[0] = strKeyValue[0].trim();
                strKeyValue[1] = strKeyValue[1].trim();
                
                if ( Properties.containsKey( strKeyValue[0] ) )
                {
                    Logger.Log( "Duplicate property \"" + strKeyValue[0] + "\" in the config file, using the last value present" );
                }
                
                Properties.put( strKeyValue[0], Integer.parseInt( strKeyValue[1] ) );
            }
            
            br.close();
            fr.close();
            
            int size = Properties.size();
            if ( size == 0 )
                return false;
            
            return true;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public void saveConfig()
    {
        try
        {
            File config = new File( fileName );
            if ( !config.exists() )
            {
                config.getParentFile().mkdirs();
                config.createNewFile();
            }
            
            int size = Properties.size();
            if ( size == 0 )
                return;
            
            FileWriter fw = new FileWriter( config );
            BufferedWriter bw = new BufferedWriter( fw );
            
            Iterator i = Properties.keySet().iterator();
            while ( i.hasNext() )
            {
                String key = ( String ) i.next();
                bw.write( key + " = " + Properties.get( key ) + "\n" );
            }
            
            bw.close();
            fw.close();
        }
        catch ( Exception e )
        {
            Logger.Log( "Failed to write the config file: " + fileName );
            e.printStackTrace();
        }
    }
    
    public int getOrCreatePropertyValue( String name, int defaultValue )
    {
        if ( Properties.containsKey( name ) )
            return ( Integer ) Properties.get( name );
        else
        {
            setPropertyValue( name, defaultValue );
            return defaultValue;
        }
    }
    
    public int getPropertyValue( String name )
    {
        if ( Properties.containsKey( name ) )
            return ( Integer ) Properties.get( name );
        else
            return 0;
    }
    
    public void setPropertyValue( String name, int value )
    {
        Properties.put( name, value );
    }
}
