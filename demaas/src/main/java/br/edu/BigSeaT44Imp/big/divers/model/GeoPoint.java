package br.edu.BigSeaT44Imp.big.divers.model;

import org.primefaces.model.map.LatLng;

public class GeoPoint {
	
	private LatLng latLng;
	private String route;

	public GeoPoint(LatLng latLng, String route) {
		if (latLng == null) {
			throw new NullPointerException("Attributes can not be null.");
		}
		
		this.latLng = latLng;
		this.route = route;
		
	}
	
	public LatLng getLatLng() {
		return latLng;
	}

	public void setLatLng(LatLng latLng) {
		this.latLng = latLng;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}
	
	public String getLatLongString() {
		return getLatLng().getLat() +","+ getLatLng().getLng();
	}
	
	public static float getDistanceInMeters(GPSPoint point1, GPSPoint point2) {
		
		return getDistanceInMeters(Double.valueOf(point1.getLatLng().getLat()), Double.valueOf(point1.getLatLng().getLng()), 
				Double.valueOf(point2.getLatLng().getLat()), Double.valueOf(point2.getLatLng().getLng()));
	}
	
	public static float getDistanceInMeters(double lat1, double lng1, double lat2, double lng2) {
		final double earthRadius = 6371000; // meters
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		float dist = (float) (earthRadius * c);

		return dist;
	}
}