package br.edu.BigSeaT44Imp.big.divers.filtro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.primefaces.model.map.LatLng;

import br.edu.BigSeaT44Imp.big.divers.model.Bus;
import br.edu.BigSeaT44Imp.big.divers.model.BusStopInfo;
import br.edu.BigSeaT44Imp.big.divers.model.ShapeLine;
import br.edu.BigSeaT44Imp.big.divers.model.ShapePoint;

public class BulmaFilter {
	
	public final static String HEADER_OUTPUT_BULMA = "TRIP_NUM,ROUTE,SHAPE_ID,SHAPE_SEQ,LAT_SHAPE,LON_SHAPE,GPS_POINT_ID,BUS_CODE,TIMESTAMP,LAT_GPS,LON_GPS,DISTANCE,THRESHOLD_PROBLEM,TRIP_PROBLEM";
	public final static String HEADER_SHAPE_FILE = "route_id,shape_id,shape_pt_lat,shape_pt_lon,shape_pt_sequence,shape_dist_traveled";
	
	private Map<String, Bus> busMap = new LinkedHashMap<String, Bus>();
	private Map<String, Map<Integer, List<BusStopInfo>>> busStopPoints = new LinkedHashMap<String, Map<Integer, List<BusStopInfo>>>();
	private Map<String, List<ShapeLine>> shapesMap = new LinkedHashMap<>();
	private List<Integer> listInitialPoints;
	private List<Integer> listFinalPoints;
	private String selectedBusCode;
	private String selectedRoute;
	private Bus selectedBus;
	private String selectedTrip;
	private String shapeFilePath;
	private Integer initialPoint;
	private Integer finalPoint;

	public void selectBus () {
		Bus selectedBus = busMap.get(this.selectedBusCode);
		setSelectedBus(selectedBus);
		setSelectedTrip(null);
		setInitialPoint(1);
		setFinalPoint(selectedBus.getNumberOfPoints());
		setListInitialPoints(populateListPoints());
		setListFinalPoints(populateListPoints());
	}

 	public void selectTrip() {
 		setInitialPoint(1);
 		setFinalPoint(selectedBus.getMapTrips().get(this.selectedTrip).size());
		setListInitialPoints(populateListPoints());
		setListFinalPoints(populateListPoints());
 	}
 	
 	public void selectFirstPoint() {
 		if (selectedTrip == null || selectedTrip.equals("")) {
 			setFinalPoint(selectedBus.getNumberOfPoints());
 		
 		} else {
 			setFinalPoint(selectedBus.getMapTrips().get(this.selectedTrip).size());
 		}
		setListFinalPoints(populateListPoints());
 	}
 	
	private List<Integer> populateListPoints() {
		List<Integer> listPoints = new ArrayList<>();
		for (int i = getInitialPoint(); i <= this.getFinalPoint(); i++) {
			listPoints.add(i);
		}
		return listPoints;
	}

	public static Map<String, Bus> sortByBusCode(Map<String, Bus> map) {
		List<Map.Entry<String, Bus>> list = new LinkedList<Map.Entry<String, Bus>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Bus>>() {

			public int compare(Map.Entry<String, Bus> o1, Map.Entry<String, Bus> o2) {
				return (o1.getValue().getBusCode()).compareTo(o2.getValue().getBusCode());
			}

		});

		Map<String, Bus> result = new LinkedHashMap<String, Bus>();
		for (Map.Entry<String, Bus> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	public void populateListGPS(String[] rowsGPS) {

		String previousBusCode = null;
		Bus currentBus = new Bus();
		String previousRoute = null;

		for (int i = 1; i < rowsGPS.length; i++) {
			String[] splittedRow = rowsGPS[i].split(",");

			if (!splittedRow[1].equals("REC")) {
				if (previousBusCode == null) {
					previousBusCode = splittedRow[7];
					previousRoute = splittedRow[1];

				} else if (!previousBusCode.equals(splittedRow[7])) {
					currentBus.setBusCode(previousBusCode);
					currentBus.setRoute(previousRoute);
					busMap.put(previousBusCode, currentBus);

					previousBusCode = splittedRow[7];
					previousRoute = splittedRow[1];
					currentBus = new Bus();
				}
				currentBus.addPointToTrip(splittedRow[0],
						new LatLng(Double.valueOf(splittedRow[9]), Double.valueOf(splittedRow[10])), splittedRow[13]);

			}
		}
		currentBus.setBusCode(previousBusCode);
		currentBus.setRoute(previousRoute);
		busMap.put(previousBusCode, currentBus);
	}
	
	public void populateListShapes(String[] rowsShape) {
		String previousShapeId = null;
		ShapeLine currentShapeLine = null;

		for (int i = 1; i < rowsShape.length; i++) {
			ShapePoint shapePoint = createShapePoint(rowsShape[i]);

			if (previousShapeId == null) {

				previousShapeId = shapePoint.getShapeId();
				currentShapeLine = new ShapeLine();

			} else if (!previousShapeId.equals(shapePoint.getShapeId())) {
				if (!shapesMap.containsKey(currentShapeLine.getRoute())) {
					shapesMap.put(currentShapeLine.getRoute(), new ArrayList<ShapeLine>());
				}
				shapesMap.get(currentShapeLine.getRoute()).add(currentShapeLine);

				previousShapeId = shapePoint.getShapeId();
				currentShapeLine = new ShapeLine();
			}

			currentShapeLine.addPoint(shapePoint);

		}
		// last shape that wasn't add to list before
		if (!shapesMap.containsKey(currentShapeLine.getRoute())) {
			shapesMap.put(currentShapeLine.getRoute(), new ArrayList<ShapeLine>());
		}
		shapesMap.get(currentShapeLine.getRoute()).add(currentShapeLine);
	}

	private ShapePoint createShapePoint(String line) {
		String[] splittedLine = line.split(",");
		String route = splittedLine[0].replaceAll("\"", "");
		String shapeId = splittedLine[1];
		String lat = splittedLine[2];
		String lng = splittedLine[3];
		//TODO
		String ss = splittedLine[4];
		Float distanceTraveled = Float.valueOf(splittedLine[5]);
		return new ShapePoint(route, shapeId, ss, new LatLng(Double.valueOf(lat), Double.valueOf(lng)), distanceTraveled);
	}
 	
	public String getSelectedRoute() {
		return selectedRoute;
	}
	
	public void setSelectedRoute(String route) {
		this.selectedRoute = route;
	}
	
	public Bus getSelectedBus() {
		return selectedBus;
	}
	
	public void setSelectedBus(Bus selectedBus) {
		this.selectedBus = selectedBus;
	}
	
	public String getSelectedTrip() {
		return selectedTrip;
	}
	
	public void setSelectedTrip(String trip) {
		this.selectedTrip = trip;
	}
	
	public String getShapeFilePath() {
		return shapeFilePath;
	}
	
	public void setShapeFilePath(String shapeFilePath) {
		this.shapeFilePath = shapeFilePath;
	}
	
	public Integer getInitialPoint() {
		return initialPoint;
	}
	
	public void setInitialPoint(Integer initialPoint) {
		this.initialPoint = initialPoint;
	}
	
	public Integer getFinalPoint() {
		return finalPoint;
	}
	
	public void setFinalPoint(Integer finalPoint) {
		this.finalPoint = finalPoint;
	}
	
	public Map<String, Bus> getBusMap() {
		return sortByBusCode(busMap);
	}

	public void setBusMap(Map<String, Bus> busMap) {
		this.busMap = busMap;
	}
	
	public List<Integer> getListInitialPoints() {
		return listInitialPoints;
	}

	public void setListInitialPoints(List<Integer> listInitialPoints) {
		this.listInitialPoints = listInitialPoints;
	}

	public List<Integer> getListFinalPoints() {
		return listFinalPoints;
	}

	public void setListFinalPoints(List<Integer> listFinalPoints) {
		this.listFinalPoints = listFinalPoints;
	}
	

	public Map<String, List<ShapeLine>> getShapesMap() {
		return shapesMap;
	}

	public void setShapesMap(Map<String, List<ShapeLine>> shapesMap) {
		this.shapesMap = shapesMap;
	}

	public String getSelectedBusCode() {
		return selectedBusCode;
	}

	public void setSelectedBusCode(String busCode) {
		this.selectedBusCode = busCode;
	}
	
	public Map<String, Map<Integer, List<BusStopInfo>>> getBusStopPoints() {
		return busStopPoints;
	}

	public void setBusStopPoints(Map<String, Map<Integer, List<BusStopInfo>>> busStopPoints) {
		this.busStopPoints = busStopPoints;
	}

	// Lucas (INICIO)
	
	//Pega o tamanho de uma rota de acordo com o shapeId que indica de a rota eh de ida ou volta
	public float getLenghtShape(String route, String shapeId) {				
		for (ShapeLine shapeLine : shapesMap.get(route)) {	
			if (shapeLine.getShapeId().equalsIgnoreCase(shapeId)) {
				return shapeLine.getShapePoints().get(shapeLine.getShapePoints().size() - 1).getDistanceTraveled();
			}
		}
		return 0;
	}
		
	//Popula a lista com os dados de paradas dos onibus
	public void populateBusStopPoints(String[] rowsBusStopPoints) {	
		for (int i = 1; i < rowsBusStopPoints.length; i++) {
			BusStopInfo busStopPoint = createBusStopPoint(rowsBusStopPoints[i]);
			
			if (!busStopPoints.containsKey(busStopPoint.getRoute())) {
				busStopPoints.put(busStopPoint.getRoute(), new LinkedHashMap<Integer, List<BusStopInfo>>());
			}
			
			if(!busStopPoints.get(busStopPoint.getRoute()).containsKey(busStopPoint.getStopSequence())) {
				busStopPoints.get(busStopPoint.getRoute()).put(busStopPoint.getStopSequence(), new LinkedList<BusStopInfo>());
			}
			if (!busStopPoints.get(busStopPoint.getRoute()).get(busStopPoint.getStopSequence()).contains(busStopPoint)) {
				busStopPoints.get(busStopPoint.getRoute()).get(busStopPoint.getStopSequence()).add(busStopPoint);
				
				Collections.sort(busStopPoints.get(busStopPoint.getRoute()).get(busStopPoint.getStopSequence()), new Comparator<BusStopInfo>() {
					
					public int compare(BusStopInfo o1, BusStopInfo o2) {
						if (o1.getSeconds() < o2.getSeconds()) {
				            return -1;
				        }
				        if (o1.getSeconds() > o2.getSeconds()) {
				            return 1;
				        }
				        return 0;
					}
				});
			}
		}
	}
	
	//Cria um ponto de parada de onibus
	private BusStopInfo createBusStopPoint(String line) {
		String[] splittedLine = line.split(",");
		
		String route = splittedLine[6];
		String lat = splittedLine[4];
		String lng = splittedLine[5];
		String arrivalTime = splittedLine[0].replaceAll("\"", "");
		String departueTime = splittedLine[1];
		String stopId = splittedLine[2];
		String stopSequence = splittedLine[3];
		String shapeId = splittedLine[7];
		String closestShapePoint = splittedLine[8];

		return new BusStopInfo(new LatLng(Double.valueOf(lat), Double.valueOf(lng)), route, arrivalTime, departueTime, stopId, Integer.valueOf(stopSequence), shapeId, closestShapePoint);
	}
	
	// Lucas (FIM)
	
}
