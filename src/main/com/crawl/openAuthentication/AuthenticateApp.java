/**
 * 
 */
package main.com.crawl.openAuthentication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import main.com.utilPackage.AuthenticationUtils;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;


/**
 * @author Piyush
 *
 */
public class AuthenticateApp 
{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AuthenticateApp app = new AuthenticateApp();
		AuthenticationData tokensecret = app.getUserAccessKeySecret();
		System.out.println(tokensecret.toString());

	}

	private AuthenticationData getUserAccessKeySecret() {
		// TODO Auto-generated method stub
		try {
			// consumer key for Twitter Data Analytics application
			if (AuthenticationUtils.CONSUMER_KEY.isEmpty()) 
			{
				System.out.println("Register your application with Twitter first then fill in the consumer key in the config file.");
				return null;
			}
			if (AuthenticationUtils.CONSUMER_SECRET.isEmpty()) 
			{
				System.out.println("Register your application with Twitter first then fill in the consumer secret in the config file.");
				return null;
			}
			
			OAuthConsumer consumer = new CommonsHttpOAuthConsumer(AuthenticationUtils.CONSUMER_KEY, AuthenticationUtils.CONSUMER_SECRET);
			
			OAuthProvider provider = new DefaultOAuthProvider(AuthenticationUtils.REQUEST_TOKEN_URL, AuthenticationUtils.ACCESS_TOKEN_URL,
															  AuthenticationUtils.AUTHORIZE_URL);
			
			String authUrl = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);
			
			System.out.println("Now open your browser and go to : \n" + authUrl + "\n authorize this APP");
			System.out.println("Enter the PIN code fetched from Twitter");
			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String pin = br.readLine();
			
			System.out.println("Fetching access token from Twitter......");
			
			provider.retrieveAccessToken(consumer, pin);
			String accesstoken = consumer.getToken();
			String accesssecret = consumer.getTokenSecret();
			AuthenticationData tokensecret = new AuthenticationData(accesstoken, accesssecret);
			
			return tokensecret;
			
		} 
		catch (OAuthNotAuthorizedException ex) 
		{
			ex.printStackTrace();
		}
		catch (OAuthMessageSignerException ex) 
		{
			ex.printStackTrace();
		}
		catch (OAuthExpectationFailedException ex) 
		{
			ex.printStackTrace();
		}
		catch (OAuthCommunicationException ex)
		{
			ex.printStackTrace();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	
	public static AuthenticationData DEBUGUserAccessSecret() {
		
		String accesstokenPS = "3072238099-xlMVDnZ6wRCUnjFFRHuzjmVag51gNkSVQJsXEvz";
		String accesssecretPS = "cnvMdJYN7fybg4HexidtQM9DyyvnAoF2PADqLgAkJouZo";
		AuthenticationData data = new AuthenticationData(accesstokenPS,
				accesssecretPS);
		return data;
	}

}
