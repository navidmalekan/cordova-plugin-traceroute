
package org.ppa.cordova.traceroute;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;



/**
 * This class contain everything needed to launch a traceroute using the ping command
 * 
 * @author Olivier Goutay
 * 
 */









public class Traceroute extends CordovaPlugin {
	private static final String FROM_PING = "From";
	private static final String SMALL_FROM_PING = "from";
	private static final String PARENTHESE_OPEN_PING = "(";
	private static final String PARENTHESE_CLOSE_PING = ")";
	private static final String EXCEED_PING = "exceed";
	private static final String UNREACHABLE_PING = "100%";

	private int ttl;
	private float elapsedTime;
	public String output = "";	
	
	
	
  public static final String TAG = "Ping";

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if ("getPingInfo".equals(action)) {
      this.ping(args, callbackContext);
      return true;
    }
    return false;
  }


	


  private void ping(JSONArray args, CallbackContext callbackContext) {
    try {



		Log.d("args", args.toString());
		if (args != null && args.length() > 0) {
        JSONArray resultList = new JSONArray();
        String ip = args.getString(0);
        int maxttl = args.getInt(1);
//		int maxttl = 1;
        JSONObject t = new JSONObject();





        try
        {
            InetAddress add = InetAddress.getByName("www.google.com");
            if(add.isReachable(3000)) System.out.println("Yes");
            else System.out.println("No");


         // Build ping command with parameters
    		Process p;
    		String command = "";

    		String format = "ping -c 1 -t "+maxttl+" ";
//    		String format = "ping -c 1 -t 0 ";
    		command = String.format(format, ttl);


    		long startTime = System.nanoTime();
    		// timeout task
    		//new TimeOutAsyncTask(this, ttl).execute();
    		// Launch command
    		p = Runtime.getRuntime().exec(command + ip);
    		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

    		// Construct the response from ping
    		String s;
    		String res = "";

    		while ((s = stdInput.readLine()) != null) {

    			res += s + "\n";
    			if (s.contains(FROM_PING) || s.contains(SMALL_FROM_PING)) {
    				// We store the elapsedTime when the line from ping comes
    				elapsedTime = (System.nanoTime() - startTime) / 1000000.0f;
    			}
    			//System.out.print("Hello");
    			//Toast.makeText(context, "in while", Toast.LENGTH_LONG).show();


    		}

    		p.destroy();






    		//output = res + "\n";
    		if (res.equals("")) {
    			callbackContext.error("domain");
    	        return;
    		}

    		// Store the wanted ip adress to compare with ping result
    		String ip1 = "";
    		if (res.contains(FROM_PING)) {
    			// Get ip when ttl exceeded
    			int index1 = res.indexOf(FROM_PING);

    			ip1 = res.substring(index1 + 5);
    			if (ip1.contains(PARENTHESE_OPEN_PING)) {
    				// Get ip when in parenthese
    				int indexOpen = ip1.indexOf(PARENTHESE_OPEN_PING);
    				int indexClose = ip1.indexOf(PARENTHESE_CLOSE_PING);

    				ip1 = ip1.substring(indexOpen + 1, indexClose);
    			} else {
    				// Get ip when after from
    				ip1 = ip1.substring(0, ip1.indexOf("\n"));
    				if (ip1.contains(":")) {
    					index1 = ip1.indexOf(":");
    				} else {
    					index1 = ip1.indexOf(" ");
    				}

    				ip1 = ip1.substring(0, index1);
    			}
    		} else {
    			// Get ip when ping succeeded
    			int indexOpen = res.indexOf(PARENTHESE_OPEN_PING);
    			int indexClose = res.indexOf(PARENTHESE_CLOSE_PING);

    			ip1 = res.substring(indexOpen + 1, indexClose);
    		}




    		if (res.contains(UNREACHABLE_PING) && !res.contains(EXCEED_PING)) {
				// Create the TracerouteContainer object when ping
				// failed
    			elapsedTime = 0;
			}


    		InetAddress inetAddr = InetAddress.getByName(ip1);
			String hostname = inetAddr.getHostName();
			String canonicalHostname = inetAddr.getCanonicalHostName();

			Log.d("hostname", "hostname : " + hostname);
			Log.d("canonicalHostname", "canonicalHostname : " + canonicalHostname);
			Log.d("res", res);
            Log.d("time", ""+elapsedTime);
			Log.d("ip", ip1);


			t.put("canonicalHostname", canonicalHostname);
    		t.put("res", res);
    		t.put("time", elapsedTime);
    		t.put("ip", ip1);

			if (res.contains("1 received")) {
        		t.put("finished", 1);
        	}

            resultList.put(t);
			Log.d("t", t.toString());
			Log.d("resultList", resultList.toString());

			callbackContext.success(resultList);
        }
        catch (UnknownHostException e)
        {
        	callbackContext.error("unkownhostexception");
        	System.out.println("unkownhostexception");
        }
        catch (IOException e)
        {
        	callbackContext.error("IoException");
            System.out.println("IoException");
        }

      } else {
        callbackContext.error("Error occurred");
      }
    } catch (Exception e) {
      LOG.e("errore try avval",""+e);
      callbackContext.error(""+e);
      System.out.println(e.getMessage());
    }
  }
}
