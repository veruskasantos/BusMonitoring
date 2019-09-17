package br.edu.BigSeaT44Imp.big.divers.model;

import org.primefaces.model.map.LatLng;

public class ShapePoint extends GeoPoint {
	
	private String ShapeId;
	private String shapeSequence;
	private Float distanceTraveled;
		
	public ShapePoint(String route, String shapeId, String shapeSequence, LatLng coord, Float distanceTraveled) {
		super(coord, route);
		this.ShapeId = shapeId;
		this.shapeSequence = shapeSequence;
		this.distanceTraveled = distanceTraveled;
	}
	
	public String getShapeId() {
		return ShapeId;
	}

	public void setShapeId(String shapeId) {
		ShapeId = shapeId;
	}

	public Float getDistanceTraveled() {
		return distanceTraveled;
	}

	public void setDistanceTraveled(Float distanceTraveled) {
		this.distanceTraveled = distanceTraveled;
	}

	public String getShapeSequence() {
		return shapeSequence;
	}

	public void setShapeSequence(String shapeSequence) {
		this.shapeSequence = shapeSequence;
	}
		
}
