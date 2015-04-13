/**
 * 
 */
package main.com.crawl.user;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.com.crawl.openAuthentication.AuthenticateApp;
import main.com.crawl.openAuthentication.AuthenticationData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

/**
 * @author Piyush
 *
 */
public class Search {
	
	// REST Search Example
	BufferedWriter OutFileWriter;
	AuthenticationData OAuthTokens;
	OAuthConsumer Consumer;
	String query = "#protest";
	String DEF_FILENAME = "searchresults.json";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Search search = new Search();
		ArrayList<String> queryterms = new ArrayList<String>();
		String outfilename = search.DEF_FILENAME;
		if (args != null)
		{
			if (args.length > 0)
			{
				for (int i = 0; i < args.length; i++) 
				{
					queryterms.add(args[i]);
				}
			} 
			else 
			{
				queryterms.add(search.query);
			}
		}
		search.LoadTwitterToken();
		search.Consumer = search.GetConsumer();
		System.out.println(search.GetRateLimitStatus());
		search.InitializeWriters(outfilename);
		JSONArray results = search.GetSearchResults(search.CreateORQuery(queryterms));
		if (results != null) 
		{
			search.WriteToFile(results);
		}
		search.CleanupAfterFinish();
	}
	
	public JSONArray GetSearchResults(String query)
	{
		try 
		{
			// construct the request url
			String URL_PARAM_SEPERATOR = "&";
			StringBuilder url = new StringBuilder();
			url.append("https://api.twitter.com/1.1/search/tweets.json?q=");
			// query needs to be encoded
			url.append(URLEncoder.encode(query, "UTF-8"));
			url.append(URL_PARAM_SEPERATOR);
			url.append("count=100");
			URL navurl = new URL(url.toString());
			HttpURLConnection huc = (HttpURLConnection) navurl.openConnection();
			huc.setReadTimeout(5000);
			Consumer.sign(huc);
			huc.connect();
			if (huc.getResponseCode() == 400 || huc.getResponseCode() == 404
					|| huc.getResponseCode() == 429) {
				System.out.println(huc.getResponseMessage());
				try 
				{
					huc.disconnect();
					Thread.sleep(this.GetWaitTime("/friends/list"));
				} 
				catch (InterruptedException ex) 
				{
					ex.printStackTrace();
				}
			}
			if (huc.getResponseCode() == 500 || huc.getResponseCode() == 502
					|| huc.getResponseCode() == 503) 
			{
				System.out.println(huc.getResponseMessage());
				try 
				{
					Thread.sleep(2000);
				}
				catch (InterruptedException ex) 
				{
					Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			BufferedReader bRead = new BufferedReader(new InputStreamReader(
					(InputStream) huc.getInputStream()));
			String temp;
			StringBuilder page = new StringBuilder();
			while ((temp = bRead.readLine()) != null) {
				page.append(temp);
			}
			JSONTokener jsonTokener = new JSONTokener(page.toString());
			try 
			{
				JSONObject json = new JSONObject(jsonTokener);
				JSONArray results = json.getJSONArray("statuses");
				return results;
			} 
			catch (JSONException ex) 
			{
				Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
			}
		} 
		catch (OAuthCommunicationException ex) 
		{
			Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
		} 
		catch (OAuthMessageSignerException ex) 
		{
			Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
		} 
		catch (OAuthExpectationFailedException ex) 
		{
			Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	
	public long GetWaitTime(String api)
	{
		JSONObject jobj = this.GetRateLimitStatus();
		if (jobj != null) 
		{
			try 
			{
				if (!jobj.isNull("resources")) 
				{
					JSONObject resourcesobj = jobj.getJSONObject("resources");
					JSONObject statusobj = resourcesobj.getJSONObject("statuses");
					JSONObject apilimit = statusobj.getJSONObject(api);
					int numremhits = apilimit.getInt("remaining");
					if (numremhits <= 1) 
					{
						long resettime = apilimit.getInt("reset");
						resettime = resettime * 1000; // convert to milliseconds
						return resettime;
					}
				}
			} 
			catch (JSONException ex)
			{
				ex.printStackTrace();
			}
		}
		return 0;
	}
	
	public void WriteToFile(JSONArray searchResults) 
	{
		try 
		{
			for (int i = 0; i < searchResults.length(); i++)
			{
				try
				{
					OutFileWriter.write(searchResults.getJSONObject(i).toString());
					OutFileWriter.newLine();
				} 
				catch (JSONException ex) 
				{
					Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public String CreateORQuery(ArrayList<String> queryTerms)
	{
		String OR_Operator = " OR ";
		StringBuffer querystr = new StringBuffer();
		int count = 1;
		for (String term : queryTerms) 
		{
			if (count == 1) 
			{
				querystr.append(term);
			} 
			else 
			{
				querystr.append(OR_Operator).append(term);
			}
		}
		return querystr.toString();
	}
	
	public void InitializeWriters(String outFilename) 
	{
		try 
		{
			File fl = new File(outFilename);
			if (!fl.exists())
			{
				fl.createNewFile();
			}
			/**
			 * Use UTF-8 encoding when saving files to avoid losing Unicode
			 * characters in the data
			 */
			OutFileWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outFilename, true), "UTF-8"));
		} 
		catch (IOException ex) 
		{
			ex.printStackTrace();
		}
	}
	
	public JSONObject GetRateLimitStatus() 
	{
		try 
		{
			URL url = new URL(
					"https://api.twitter.com/1.1/application/rate_limit_status.json");
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setReadTimeout(5000);
			OAuthConsumer consumer = new DefaultOAuthConsumer(
					main.com.utilPackage.AuthenticationUtils.CONSUMER_KEY,
					main.com.utilPackage.AuthenticationUtils.CONSUMER_SECRET);
			consumer.setTokenWithSecret(OAuthTokens.getAccessToken(), OAuthTokens.getAccessSecret());
			consumer.sign(huc);
			huc.connect();
			
			BufferedReader bRead = new BufferedReader(new InputStreamReader(
					(InputStream) huc.getContent()));
			StringBuffer page = new StringBuffer();
			String temp = "";
			
			while ((temp = bRead.readLine()) != null) 
			{
				page.append(temp);
			}
			bRead.close();
			return (new JSONObject(page.toString()));
		} 
		catch (JSONException ex) 
		{
			Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch (OAuthCommunicationException ex) 
		{
			Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch (OAuthMessageSignerException ex) 
		{
			Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
		} 
		catch (OAuthExpectationFailedException ex)
		{
			Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
		} 
		catch (IOException ex)
		{
			Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	
	public void LoadTwitterToken()
	{
		// Un-comment before release
		// OAuthExample oae = new OAuthExample();
		// OAuthTokens = oae.GetUserAccessKeySecret();
		// Remove before release
		OAuthTokens = AuthenticateApp.DEBUGUserAccessSecret();
	}
	
	public OAuthConsumer GetConsumer() 
	{
		OAuthConsumer consumer = new DefaultOAuthConsumer(
				main.com.utilPackage.AuthenticationUtils.CONSUMER_KEY, main.com.utilPackage.AuthenticationUtils.CONSUMER_SECRET);
		consumer.setTokenWithSecret(OAuthTokens.getAccessToken(),
				OAuthTokens.getAccessSecret());
		return consumer;
	}
	
	/**
	 * Close the opened filewriter to save the data
	 */
	public void CleanupAfterFinish()
	{
		try 
		{
			OutFileWriter.close();
		} 
		catch (IOException ex)
		{
			Logger.getLogger(UserInfo.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
