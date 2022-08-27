package com.oracle.ondb.examples.utility;

import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 * @author rcgreene
 *
 */
public class CommIO{

	public static int c = 0;
	
    public String getUserInput( String why ){
    	System.out.println( why );
    	return getUserInput( );
    }
    
    public String getUserInput(){
        InputStreamReader  input = new InputStreamReader(System.in);
        BufferedReader stream = new BufferedReader( input );
        String answer = new String();

        try{
            answer = stream.readLine();
        }
        catch (java.io.IOException e){
            return answer;
        }
        return answer;
    }
    
    public static void suspend(String why ){
    	System.out.println( why );
    	suspend();
    }
    
    public static void suspend(){
        CommIO input = new CommIO();
        input.getUserInput();
    }
    
    public void say( String said ){
    	System.out.println(said);
    }
    
    public String question( String asked ){
    	return getUserInput(asked);
    }
   

    public int getInt( String question ){
    	int value = 0;
    	String sVal = null;
    	boolean succeed = false;
    	
    	while( !succeed ){
    		sVal = question(question);
    		try{
    			value = Integer.valueOf(sVal).intValue();
    			succeed = true;
    		}catch( NumberFormatException ne ){
    			say("You need to enter an integer number. Try again.");
    		}
    	}
    	c = value;
    	return c;
    }

}

