package br.edu.BigSeaT44Imp.big.divers.model;

import java.util.ArrayList;
import java.util.List;

public class BusProgressModel implements Comparable<BusProgressModel>{
	
private static final String NO_TIME_COLOR = "pink";
	
	private List<ShapeLine> shapes;
	private String busCode;
	private String route;
	private String situation;
	private Integer progress;
	private float totalReadPoints;
	
	public BusProgressModel(String busCode, String situation, List<ShapeLine> shapes, Integer currentTotalReadPoints) {
		this.busCode = busCode;
		this.shapes = shapes;
		this.situation = situation;
		this.setRoute("");
		this.setTotalReadPoints(currentTotalReadPoints);
	}
	
	public BusProgressModel(String busCode, Integer currentTotalReadPoints) {
		this(busCode, NO_TIME_COLOR, new ArrayList<ShapeLine>(), currentTotalReadPoints);
	}
	
	private float getNumShapePoints() {
		float totalPoints = 0;
		for (ShapeLine shapeLine : shapes) {
			totalPoints += shapeLine.getShapePoints().size();
		}
		
		return totalPoints;
	}
	
	public Integer getProgress() {

		float numPoints = getNumShapePoints();
		if (numPoints > 0) {
			float sum = this.totalReadPoints / numPoints;

			progress = (int) (sum * 100);

			if (progress > 100)
				progress = 100;
		}

		return progress;
	}

	public void setProgress(Integer progress) {
		this.progress = progress;
	}

	public void cancel() {
		progress = null;
	}

	public List<ShapeLine> getShapes() {
		return shapes;
	}

	public void setShapes(List<ShapeLine> shapes) {
		this.shapes = shapes;
	}

	public String getBusCode() {
		return busCode;
	}

	public void setBusCode(String busCode) {
		this.busCode = busCode;
	}

	public float getTotalReadPoints() {
		return totalReadPoints;
	}

	public void setTotalReadPoints(float totalReadPoints) {
		this.totalReadPoints = totalReadPoints;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((busCode == null) ? 0 : busCode.hashCode());
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
		BusProgressModel other = (BusProgressModel) obj;
		if (busCode == null) {
			if (other.busCode != null)
				return false;
		} else if (!busCode.equals(other.busCode))
			return false;
		return true;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		if (!getShapes().isEmpty()) {
			this.route = getShapes().get(0).getRoute();
		}
	}

	@Override
	public int compareTo(BusProgressModel o) {
		return this.route.compareTo(o.getRoute());
	}

	public String getSituation() {
		return situation;
	}

	public void setSituation(String situation) {
		this.situation = situation;
	}
}