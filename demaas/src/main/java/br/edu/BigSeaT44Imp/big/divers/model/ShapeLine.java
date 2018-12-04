package br.edu.BigSeaT44Imp.big.divers.model;

import java.util.LinkedList;
import java.util.List;

public class ShapeLine {
	
	private List<ShapePoint> shapePoints;
	private String route;
	private String color;
	private String shapeId;
	
	public ShapeLine() {
		this.shapePoints = new LinkedList<ShapePoint>();
	}

	public void addPoint(ShapePoint shapePoint) {
		
		// TODO melhorar a forma de pegar a rota aqui
		if (route == null) {
			this.route  = shapePoint.getRoute();
		}
		
		if (shapeId == null) {
			this.shapeId = shapePoint.getShapeId();
		}
		 
		this.shapePoints.add(shapePoint);
	}
	
	public List<ShapePoint> getShapePoints() {
		return shapePoints;
	}

	public void setShapePoints(List<ShapePoint> shapePoints) {
		this.shapePoints = shapePoints;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public String getColor() {
		return this.color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getShapeId() {
		return shapeId;
	}

	public void setShapeId(String shapeId) {
		this.shapeId = shapeId;
	}
}
