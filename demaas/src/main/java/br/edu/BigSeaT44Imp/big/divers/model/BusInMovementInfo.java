package br.edu.BigSeaT44Imp.big.divers.model;

public class BusInMovementInfo {
	
	private GPSPoint penultimateGPSPoint;
	private GPSPoint lastGPSPoint;
	private String shapeSequence;
	private long lastTime;
	
	public BusInMovementInfo(GPSPoint penultimateGPSPoint, GPSPoint lastGPSPoint, String shapeSequence, long lastTime) {
		super();
		this.penultimateGPSPoint = penultimateGPSPoint;
		this.lastGPSPoint = lastGPSPoint;
		this.shapeSequence = shapeSequence;
		this.lastTime = lastTime;
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
		return shapeSequence;
	}

	public void setShapeSequence(String shapeSequence) {
		this.shapeSequence = shapeSequence;
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
		result = prime * result + ((shapeSequence == null) ? 0 : shapeSequence.hashCode());
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
		if (shapeSequence == null) {
			if (other.shapeSequence != null)
				return false;
		} else if (!shapeSequence.equals(other.shapeSequence))
			return false;
		return true;
	}
	
}
