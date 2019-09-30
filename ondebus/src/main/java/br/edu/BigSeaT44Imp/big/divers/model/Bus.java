package br.edu.BigSeaT44Imp.big.divers.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.primefaces.model.map.LatLng;

public class Bus implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String busCode;
	private Map<String, List<GPSPoint>> mapTrips;
	private String route;
	private List<Integer> listNumberPoints;
	private int numberOfPoints;
	
	public Bus() {
		this.mapTrips = new LinkedHashMap<>();
		this.listNumberPoints = new LinkedList<>();
		this.numberOfPoints = 0;
	}

	public String getBusCode() {
		return busCode;
	}

	public void setBusCode(String busCode) {
		this.busCode = busCode;
	}
	
	public Map<String, List<GPSPoint>> getMapTrips() {
		return mapTrips;
	}

	public void setMapTrips(Map<String, List<GPSPoint>> mapTrips) {
		this.mapTrips = mapTrips;
	}
	
	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public List<Integer> getListNumberPoints() {
		return listNumberPoints;
	}

	public void setListNumberPoints(List<Integer> listNumberPoints) {
		this.listNumberPoints = listNumberPoints;
	}

	public int getNumberOfPoints() {
		return numberOfPoints;
	}

	public void setNumberOfPoints(int numberOfPoints) {
		this.numberOfPoints = numberOfPoints;
	}

	public void addPointToTrip(String trip, LatLng coord, String problem) {
		
		if (!this.mapTrips.containsKey(trip)) {
			this.mapTrips.put(trip, new ArrayList<GPSPoint>());
		}
 
		//this.mapTrips.get(trip).add(new GPSPoint(coord, problem, null, null, null, null, null, null, null, null, null)); TODO line used before ondebus
		this.mapTrips.get(trip).add(new GPSPoint(coord, null, null, null, null, null, null, null, null, null, null, null));
		this.numberOfPoints++;
		this.listNumberPoints.add(numberOfPoints);
	}
	
	
}
