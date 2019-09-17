package br.edu.BigSeaT44Imp.big.divers.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.primefaces.model.map.LatLng;

public class GPSPoint extends GeoPoint implements Serializable {

	private static final long serialVersionUID = 1L;

	private String timeStamp;
	private String shapeSequence;
	private String shapeId;
	private String busCode;
	private Double distanceTraveled;
	private String problem;
	private String expectedTime;
	private String currentTime;
	private String situation;

	public GPSPoint(LatLng latLng, String problem, String busCode, String timestamp, String shapeId,
			String shapeSequence, Double distanceTraveled, String route, String expectedTime, String currentTime, String situation ) {
		super(latLng, route);
		this.problem = problem;
		this.busCode = busCode;
		this.timeStamp = timestamp;
		this.shapeId = shapeId;
		this.shapeSequence = shapeSequence;
		this.distanceTraveled = distanceTraveled;
		this.expectedTime = expectedTime;
		this.currentTime = currentTime;
		this.situation = situation;
		
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public boolean hasProblem() {
		if (problem.contains("0")) {
			return false;
		}
		return true;
	}
	
	public boolean isAboveThreshold() {
		if (problem.contains("-3")) {
			return true;
		}
		return false;
	}
	
	public String getProblem() {
		return problem;
	}
	
	public void setProblem(String problem) {
		this.problem = problem;
	}
	

	public long getTime() {
		
		SimpleDateFormat sdft = new SimpleDateFormat("hh:mm:ss");
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTimeInMillis(sdft.parse(this.timeStamp.trim()).getTime());
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Calendar caltmp = Calendar.getInstance();
		cal.set(caltmp.get(Calendar.YEAR), caltmp.get(Calendar.MONTH), caltmp.get(Calendar.DAY_OF_MONTH));
		
		return cal.get(Calendar.SECOND) + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.HOUR_OF_DAY) * 3600;
	}

	public static float getDistanceInMeters(GPSPoint point1, GPSPoint point2) {

		return getDistanceInMeters(Double.valueOf(point1.getLatLng().getLat()),
				Double.valueOf(point1.getLatLng().getLng()), Double.valueOf(point2.getLatLng().getLat()),
				Double.valueOf(point2.getLatLng().getLng()));
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

	@Override
	public String toString() {
		return "GPSPoint [latLong=" + getLatLng() + ", situation=" + getSituation() + ", current time=" + getCurrentTime() + ", expected time=" + getExpectedTime() + ", code=" + getBusCode() + ", route=" + getRoute() + ", shapeId=" + getShapeId() + ", shape sequence=" + getShapeSequence() + "]";
	}

	public String getShapeSequence() {
		return shapeSequence;
	}

	public void setShapeSequence(String shapeSequence) {
		this.shapeSequence = shapeSequence;
	}

	public String getShapeId() {
		return shapeId;
	}

	public void setShapeId(String shapeId) {
		this.shapeId = shapeId;
	}

	public String getBusCode() {
		return busCode;
	}

	public void setBusCode(String busCode) {
		this.busCode = busCode;
	}

	public Double getDistanceTraveled() {
		return distanceTraveled;
	}

	public void setDistanceTraveled(Double distanceTraveled) {
		this.distanceTraveled = distanceTraveled;
	}

	public String getExpectedTime() {
		return expectedTime;
	}

	public void setExpectedTime(String expectedTime) {
		this.expectedTime = expectedTime;
	}

	public String getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(String currentTime) {
		this.currentTime = currentTime;
	}

	public String getSituation() {
		return situation;
	}

	public void setSituation(String situation) {
		this.situation = situation;
	}
	
	public float getSeconds() {
		String[] rowsTimeStamp = currentTime.split(":");
		
		return Float.valueOf(rowsTimeStamp[0]) * 3600 + Float.valueOf(rowsTimeStamp[1]) * 60 + Float.valueOf(rowsTimeStamp[2]);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((busCode == null) ? 0 : busCode.hashCode());
		result = prime * result + ((shapeId == null) ? 0 : shapeId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof GPSPoint))
			return false;
		GPSPoint other = (GPSPoint) obj;
		if (busCode == null) {
			if (other.busCode != null)
				return false;
		} else if (!busCode.equals(other.busCode))
			return false;
		if (shapeId == null) {
			if (other.shapeId != null)
				return false;
		} else if (!shapeId.equals(other.shapeId))
			return false;
		return true;
	}
	
}