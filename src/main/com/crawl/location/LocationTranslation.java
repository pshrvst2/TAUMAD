/**
 * 
 */
package main.com.crawl.location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;

import main.com.crawl.user.Location;

/**
 * @author Piyush
 *
 */
public class LocationTranslation {

	/**
	 * @param args
	 */
	
	public Location TranslateLoc(String loc) 
	{
		if (loc != null && !loc.isEmpty()) {
			String encodedLoc = "";
			try 
			{
				// Step 1: Encode the location name
				encodedLoc = URLEncoder.encode(loc, "UTF-8");
			} 
			catch (UnsupportedEncodingException ex) 
			{
				Logger.getLogger(LocationTranslation.class.getName()).log(Level.SEVERE, null, ex);
			}
			// Step 2: Create a get request to MapQuest API with the name of the
			// location
			String url = "http://open.mapquestapi.com/nominatim/v1/search?q="
					+ encodedLoc + "&format=json";
			String page = ReadHTML(url);
			if (page != null) {
				try {
					JSONArray results = new JSONArray(page);
					if (results.length() > 0) {
						// Step 3: Read and extract the coordinates of the
						// location as a JSONObject
						Location loca = new Location(results.getJSONObject(0)
								.getDouble("lat"), results.getJSONObject(0)
								.getDouble("lon"));
						return loca;
					}
				} catch (JSONException ex) {
					Logger.getLogger(LocationTranslation.class.getName())
							.log(Level.SEVERE, null, ex);
				}
			}
		}
		return null;
	}
	
	public String ReadHTML(String url) {
		URLConnection conn = null;
		URL theURL = null;
		try {
			theURL = new URL(url);
		} catch (MalformedURLException e) {
			System.out.println("Bad URL: " + theURL);
			return null;
		}
		String page = "";
		try {
			conn = theURL.openConnection();
			HttpURLConnection huc = (HttpURLConnection) conn;
			conn.setConnectTimeout(2000);
			huc.setRequestProperty("User-Agent", "Mozilla/4.5");
			// Set your email address in the request so MapQuest knows how to
			// reach you in the event of problems
			huc.setRequestProperty("Email", "twitterdataanalytics@gmail.com");
			if (huc.getResponseCode() >= 400 && huc.getResponseCode() <= 404) {
				return null;
			}
			conn.connect();
			BufferedReader bRead = new BufferedReader(new InputStreamReader(
					(InputStream) conn.getContent()));
			String temp = null;
			while ((temp = bRead.readLine()) != null) {
				page = page + "\n" + temp;
			}
			bRead.close();
		} catch (IOException e) {
			// System.out.print("ReadHTML IO Error:" + e.getMessage()+" \n");
			return null;
		}
		return page;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LocationTranslation lte = new LocationTranslation();
		if (args != null) {
			if (args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					System.out.println(lte.TranslateLoc(args[i]).toString());
				}
			}
		}

	}

}
