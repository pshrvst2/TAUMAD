/**
 * 
 */
package main.com.crawl.user;

/**
 * @author Piyush
 *
 */
public class Location 
{
	public Double latitude;
	public Double longitude;

	public Location(Double lat, Double lng) {
		latitude = lat;
		longitude = lng;
	}

	public String toString()
	{
		return "Latitude: " + latitude + " & Longitude: " + longitude;
	}
}
