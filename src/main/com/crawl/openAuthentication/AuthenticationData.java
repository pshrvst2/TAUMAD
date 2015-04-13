/**
 * 
 */
package main.com.crawl.openAuthentication;

/**
 * @author Piyush
 *
 */
public class AuthenticationData {
	

	String accessToken;
	String accessSecret;
	
	public AuthenticationData(String accessToken, String accessSecret) {
		//super();
		this.accessToken = accessToken;
		this.accessSecret = accessSecret;
	}
	
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getAccessSecret() {
		return accessSecret;
	}

	public void setAccessSecret(String accessSecret) {
		this.accessSecret = accessSecret;
	}

	public String toString() {
		return "Access Token: " + getAccessToken() + " Access Secret: "
				+ getAccessSecret();
	}

}
