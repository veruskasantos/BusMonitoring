package br.edu.BigSeaT44Imp.big.divers.model;

import org.primefaces.model.map.LatLng;

public class BusInMovementInfo {
	
	private GPSPoint penultimateGPSPoint;
	private GPSPoint lastGPSPoint;
	private String currentShapeSequence;
	private long lastTime;
	private LatLng lastLatLng;
	private double lastDistanceTraveled;
	
	public BusInMovementInfo(GPSPoint penultimateGPSPoint, GPSPoint lastGPSPoint, String shapeSequence, long lastTime) {
		super();
		this.penultimateGPSPoint = penultimateGPSPoint;
		this.lastGPSPoint = lastGPSPoint;
		this.currentShapeSequence = shapeSequence;
		this.lastTime = lastTime;
		this.lastLatLng = lastGPSPoint.getLatLongShape();
		this.lastDistanceTraveled = lastGPSPoint.getDistanceTraveled();
	}
	
	public String getLastLatLongString() {
		return lastLatLng.getLat() +","+ lastLatLng.getLng();
	}
	
	public LatLng getLastLatLng() {
		return lastLatLng;
	}

	public void setLastLatLng(LatLng lastLatLng) {
		this.lastLatLng = lastLatLng;
	}

	public double getLastDistanceTraveled() {
		return lastDistanceTraveled;
	}

	public void setLastDistanceTraveled(double lastDistanceTraveled) {
		this.lastDistanceTraveled = lastDistanceTraveled;
	}

	public GPSPoint getPenultimateGPSPoint() {
		return penultimateGPSPoint;
	}

	public void setPenultimateGPSPoint(GPSPoint penultimateGPSPoint) {
		this.penultimateGPSPoint = penultimateGPSPoint;
	}

	public GPSPoint getLastGPSPoint() {
		return lastGPSPoint;
	}

	public void setLastGPSPoint(GPSPoint lastGPSPoint) {
		this.lastGPSPoint = lastGPSPoint;
	}

	public String getShapeSequence() {
		return currentShapeSequence;
	}

	public void setShapeSequence(String shapeSequence) {
		this.currentShapeSequence = shapeSequence;
	}
	
	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lastGPSPoint == null) ? 0 : lastGPSPoint.hashCode());
		result = prime * result + ((penultimateGPSPoint == null) ? 0 : penultimateGPSPoint.hashCode());
		result = prime * result + ((currentShapeSequence == null) ? 0 : currentShapeSequence.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BusInMovementInfo other = (BusInMovementInfo) obj;
		if (lastGPSPoint == null) {
			if (other.lastGPSPoint != null)
				return false;
		} else if (!lastGPSPoint.equals(other.lastGPSPoint))
			return false;
		if (penultimateGPSPoint == null) {
			if (other.penultimateGPSPoint != null)
				return false;
		} else if (!penultimateGPSPoint.equals(other.penultimateGPSPoint))
			return false;
		if (currentShapeSequence == null) {
			if (other.currentShapeSequence != null)
				return false;
		} else if (!currentShapeSequence.equals(other.currentShapeSequence))
			return false;
		return true;
	}

	public float getLastShapeSequence() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
