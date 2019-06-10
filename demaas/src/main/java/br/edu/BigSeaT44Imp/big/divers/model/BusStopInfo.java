package br.edu.BigSeaT44Imp.big.divers.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.primefaces.model.map.LatLng;

/**
 * 
 * @author Lucas
 *
 */
public class BusStopInfo extends GeoPoint implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String arrivalTime;
	private String departueTime;
	private String stopId;
	private int stopSequence;
	private String shapeId;
	private String closestShapePoint;

	public BusStopInfo(LatLng latLng, String route, String arrivalTime, String departueTime, String stopId, int stopSequence, String shapeId, String closestShapePoint) {
		super(latLng, route);
		this.arrivalTime = arrivalTime;
		this.departueTime = departueTime;
		this.stopId = stopId;
		this.stopSequence = stopSequence;
		this.shapeId = shapeId;
		this.closestShapePoint = closestShapePoint;
	}
	
	public long getTime() {
		
		SimpleDateFormat sdft = new SimpleDateFormat("hh:mm:ss");
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTimeInMillis(sdft.parse(this.arrivalTime.trim()).getTime());
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Calendar caltmp = Calendar.getInstance();
		cal.set(caltmp.get(Calendar.YEAR), caltmp.get(Calendar.MONTH), caltmp.get(Calendar.DAY_OF_MONTH));
		
		return cal.get(Calendar.SECOND) + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.HOUR_OF_DAY) * 3600;
	}

	public String getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public String getDepartueTime() {
		return departueTime;
	}

	public void setDepartueTime(String departueTime) {
		this.departueTime = departueTime;
	}

	public String getStopId() {
		return stopId;
	}

	public void setStopId(String stopId) {
		this.stopId = stopId;
	}

	public int getStopSequence() {
		return stopSequence;
	}

	public void setStopSequence(int stopSequence) {
		this.stopSequence = stopSequence;
	}

	public String getShapeId() {
		return shapeId;
	}

	public void setShapeId(String shapeId) {
		this.shapeId = shapeId;
	}

	public String getClosestShapePoint() {
		return closestShapePoint;
	}

	public void setClosestShapePoint(String closestShapePoint) {
		this.closestShapePoint = closestShapePoint;
	}
	
	@Override
	public String toString() {
		return "BusStopPoint [latLong=" + getLatLng() + ", arrivalTime=" + getArrivalTime() + ", stopId=" + getStopId() + ", stopSequence=" + getStopSequence() + ", route=" + getRoute() + ", shapeId=" + getShapeId() + ", closestShapePoint=" + getClosestShapePoint() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arrivalTime == null) ? 0 : arrivalTime.hashCode());
		result = prime * result + ((closestShapePoint == null) ? 0 : closestShapePoint.hashCode());
		result = prime * result + ((shapeId == null) ? 0 : shapeId.hashCode());
		result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
		result = prime * result + stopSequence;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BusStopInfo))
			return false;
		BusStopInfo other = (BusStopInfo) obj;
		if (arrivalTime == null) {
			if (other.arrivalTime != null)
				return false;
		} else if (!arrivalTime.equals(other.arrivalTime))
			return false;
		if (closestShapePoint == null) {
			if (other.closestShapePoint != null)
				return false;
		} else if (!closestShapePoint.equals(other.closestShapePoint))
			return false;
		if (shapeId == null) {
			if (other.shapeId != null)
				return false;
		} else if (!shapeId.equals(other.shapeId))
			return false;
		if (stopId == null) {
			if (other.stopId != null)
				return false;
		} else if (!stopId.equals(other.stopId))
			return false;
		if (stopSequence != other.stopSequence)
			return false;
		return true;
	}

	public float getSeconds() {
		String[] rowsTimeStamp = arrivalTime.split(":");
		
		return Float.valueOf(rowsTimeStamp[0]) * 3600 + Float.valueOf(rowsTimeStamp[1]) * 60 + Float.valueOf(rowsTimeStamp[2]);
	}

}