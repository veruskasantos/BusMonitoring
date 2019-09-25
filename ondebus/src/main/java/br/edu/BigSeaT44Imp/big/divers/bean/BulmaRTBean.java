package br.edu.BigSeaT44Imp.big.divers.bean;

import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Polyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import br.edu.BigSeaT44Imp.big.divers.filtro.BulmaFilter;
import br.edu.BigSeaT44Imp.big.divers.model.BusHeadwayInfo;
import br.edu.BigSeaT44Imp.big.divers.model.BusInMovementInfo;
import br.edu.BigSeaT44Imp.big.divers.model.BusStopInfo;
import br.edu.BigSeaT44Imp.big.divers.model.FileNode;
import br.edu.BigSeaT44Imp.big.divers.model.GPSPoint;
import br.edu.BigSeaT44Imp.big.divers.model.GeoPoint;
import br.edu.BigSeaT44Imp.big.divers.model.Pairwise;
import br.edu.BigSeaT44Imp.big.divers.model.ShapeLine;
import br.edu.BigSeaT44Imp.big.divers.model.ShapePoint;
import br.edu.BigSeaT44Imp.big.divers.service.FileService;

@Component
@Lazy
@Scope("session")
@SessionScoped
@ManagedBean
public class BulmaRTBean extends AbstractBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final String TYPE_DIRECTORY = "DIRECTORY";

	private String centerMap;
	private int zoomMap;
	private MapModel mapModel;
	private TreeNode folders;
	private String selectedPath;
	private TreeNode selectedNode;
	private TreeNode lastSelectedNode;
	private long lastModificationTime;
	private BulmaFilter bulmaFilter;

	private Map<String, GPSPoint> newMapPoints;
	private List<String> listBusCode;
	private List<String> listRoutes;
	private String selectedRoute;
	private String selectedBusCode;
	private String selectedDetection;
	private boolean bbDetection;

	// bus_code, last_gps_position
	private Map<String, BusInMovementInfo> busInMoviment;
	private Map<String, String> busCodeRoute;
	
	private Map<String, String> bbBusCodeRoute;
	private Map<String, Map<BusStopInfo, List<BusHeadwayInfo>>> bbBusHeadwayInfo = new LinkedHashMap<String, Map<BusStopInfo, List<BusHeadwayInfo>>>();
	private Map<String, List<Pairwise<BusHeadwayInfo, BusHeadwayInfo>>> bbHeadway = new LinkedHashMap<String, List<Pairwise<BusHeadwayInfo, BusHeadwayInfo>>>();
	private Map<String, List<Pairwise<GPSPoint, GPSPoint>>> bbDistance = new LinkedHashMap<String, List<Pairwise<GPSPoint,GPSPoint>>>();
	
	@Autowired
	private FileService service;
	private Integer count;
	private Integer bbCount;

	//
	private static String[] CITIES_NAME = {"Campina Grande", "Curitiba"};
	private static float THRESHOLD_VELOCITY = 0.6f;
	private String shapePath;
	private static final String LINE_SEPARATOR = "\n";
	
	@PostConstruct
	public void init() {

		setCenterMap("-7.228448, -35.881222");
		setZoomMap(15);
		mapModel = new DefaultMapModel();

		lastModificationTime = System.currentTimeMillis();
		newMapPoints = new HashMap<>();
		busInMoviment = new HashMap<>();
		busCodeRoute = new HashMap<>();
		bulmaFilter = new BulmaFilter();
		
		count = 0;
		bbCount = 0;
		
		selectedDetection = "Distance";

		listBusCode = new ArrayList<>();
		listRoutes = new ArrayList<>();
		bbBusCodeRoute = new HashMap<>();
		
		populateCitiesNameList();
	}
	
	public void onPageLoad() {
//		RequestContext.getCurrentInstance().addCallbackParam("newMarkers", new Gson().toJson(""));
//		cleanMap();
//		bbBusCodeRoute.clear();
//		bbDetection = false;
	}

	public void populateCitiesNameList() {
		
		TreeNode root = new DefaultTreeNode(new FileNode("", "-", TYPE_DIRECTORY), null);
		
		for (String city : CITIES_NAME) {
			@SuppressWarnings("unused")
			TreeNode node = new DefaultTreeNode(
					new FileNode(city, "-", TYPE_DIRECTORY), root);
		}
		
		this.folders = root;
	}

	/*public void deleteDirectory(String path) {
		JSONObject jsonEntity = service.delete("deleteDirectory", path);
		try {
			String data = jsonEntity.get("data").toString();
			JSONObject dataJson = new JSONObject(data);

			if (!jsonEntity.get("code").toString().equals("200") || !((boolean) dataJson.get("boolean"))) {
				System.err.println(jsonEntity.get("mesg").toString());
			}
		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getMessage());
		}
	}*/
	
	/**
	 * End Progress Bus
	 */

	public void selectRoute() {
		listBusCode = new ArrayList<>();
		
		for ( Entry<String, String> entry : busCodeRoute.entrySet()) {
			if (!getSelectedRoute().equals("") && entry.getValue().equals(getSelectedRoute())) {
				listBusCode.add(entry.getKey());

			} else if (getSelectedRoute().equals("")) {
				listBusCode.add(entry.getKey());
			}
		}	
	}

	
	// Get the possible next shape point based on the last distance/time (velocity)
	private LatLng getNextClosestShapePoint(Double velocity, BusInMovementInfo busIMInfo) {
		long systemTime = System.currentTimeMillis();
//		long busTime = busIMInfo.getLastGPSPoint().getTime();
		long busTime = busIMInfo.getLastTime();

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hours = cal.get(Calendar.HOUR_OF_DAY);
		int minutes = cal.get(Calendar.MINUTE);
		int seconds = cal.get(Calendar.SECOND);

		String dateInString = day + "-" + month + "-" + year + " " + hours + ":" + minutes + ":" + seconds;

		try {
			systemTime = sdf.parse(dateInString).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		systemTime = cal.get(Calendar.SECOND) + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.HOUR_OF_DAY) * 3600;

		long currentDeltaTime = systemTime - busTime;

		Double distanceOnCurrentTime = velocity * currentDeltaTime;

		float lastDistanceTraveled = 0;
		float nextDistanceTraveled = 0;

		String currentRoute = busIMInfo.getLastGPSPoint().getRoute();
		if (currentRoute.equals("-")) {
			return busIMInfo.getLastGPSPoint().getLatLng();
		}

		for (ShapeLine shapeLine : bulmaFilter.getShapesMap().get(currentRoute)) {
			if (shapeLine.getShapeId().equals(busIMInfo.getLastGPSPoint().getShapeId())) {
				String lastShapeSequence = "";
				for (ShapePoint currentShapePoint : shapeLine.getShapePoints()) {
					// TODO migrar para Map
					String shapeSequence = currentShapePoint.getShapeSequence();

					if (currentShapePoint.getShapeSequence().equals(busIMInfo.getShapeSequence())) {
						lastDistanceTraveled = currentShapePoint.getDistanceTraveled();
						lastShapeSequence = shapeSequence;

					} else if (!lastShapeSequence.isEmpty() && currentShapePoint.getShapeSequence()
							.equals(String.valueOf(Long.valueOf(lastShapeSequence) + 1))) {
						nextDistanceTraveled = currentShapePoint.getDistanceTraveled();

						float currentDeltaDistance = nextDistanceTraveled - lastDistanceTraveled;

						// TODO Mudar essa condição abaixo para refletir o
						// cenário correto
						
						// Move the bus if the calculated travelled distance >= next shape point distance
						if (distanceOnCurrentTime >= currentDeltaDistance) {
							busIMInfo.setShapeSequence(shapeSequence);
							// TODO Mudar a linha abaixo
//							busIMInfo.getLastGPSPoint().setShapeSequence(shapeSequence);
							busIMInfo.getPenultimateGPSPoint().setLatLng(busIMInfo.getLastGPSPoint().getLatLng());
							
							busIMInfo.getLastGPSPoint().setLatLng(currentShapePoint.getLatLng());
							busIMInfo.setLastTime(systemTime);
							busInMoviment.put(busIMInfo.getLastGPSPoint().getBusCode(), busIMInfo);

							return currentShapePoint.getLatLng();
						}
					}
				}
			}
		}
		return null;
	}

	public void ajaxPoll() {
		if (count++ == 10) {
			count = 0;

			checkNewGPSPoints();
		}
		
		Marker[] newMarkers = null;
		if (mapModel.getMarkers().size() > 0) {
			newMarkers = new Marker[mapModel.getMarkers().size()];
		}
		if (newMarkers != null) {
			for (int i = 0; i < newMarkers.length; i++) {
				Marker newMarker = mapModel.getMarkers().get(i);
				String keyMap = newMarker.getTitle();
				if (newMapPoints.containsKey(keyMap)) {
					// update position
					GPSPoint gpsTmp = newMapPoints.get(keyMap);

					if (busInMoviment.get(keyMap).getLastGPSPoint() != null
							&& busInMoviment.get(keyMap).getPenultimateGPSPoint() != null) {
						newMapPoints.remove(keyMap);
					} else if (!gpsTmp.getTimeStamp()
							.equals(busInMoviment.get(keyMap).getLastGPSPoint().getTimeStamp())) {
						busInMoviment.put(keyMap,
								new BusInMovementInfo(busInMoviment.get(keyMap).getLastGPSPoint(), gpsTmp,
										gpsTmp.getShapeSequence(),
										busInMoviment.get(keyMap).getLastGPSPoint().getTime()));
						newMapPoints.remove(keyMap);
					}

					newMarker.setLatlng(gpsTmp.getLatLng());
				}

				if (!newMapPoints.containsKey(keyMap)) {
					// call progress

					LatLng nextLatLng = performVirtualProgress(busInMoviment.get(keyMap));
					if (nextLatLng != null) {
						newMarker.setLatlng(nextLatLng);
					}
				}
				
				newMarker = setMarkerContent(newMarker, busInMoviment.get(keyMap).getLastGPSPoint());
				newMarkers[i] = newMarker;
			}
			
			bbDetection();
			
			RequestContext.getCurrentInstance().addCallbackParam("newMarkers", new Gson().toJson(newMarkers));
		} else {
			RequestContext.getCurrentInstance().addCallbackParam("newMarkers", new Gson().toJson(""));
		}

	}
	
	// Lucas (INICIO)
	
    public void addMessage() {
        String summary = bbDetection ? "Checked" : "Unchecked";
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(summary));
    }
	
	private void bbDetection() {
		if (bbDetection) {
			if (selectedDetection.equalsIgnoreCase("Distance")) {
				if (!bbBusCodeRoute.isEmpty()) {
					if (bbCount++ == 50) {	bbCount = 0; bbBusCodeRoute.clear(); }
				}
				bbDistanceDetection();
			}
			if (selectedDetection.equalsIgnoreCase("Headway")) {
				if (!bbBusCodeRoute.isEmpty()) {
					if (bbCount++ == 300) {	bbCount = 0; bbBusCodeRoute.clear(); }
				}
				
				bbHeadwayDetection();
			}
		}
	}
	
	private void bbHeadwayDetection() {
		
		for (Entry<String, BusInMovementInfo> entry : busInMoviment.entrySet()) {
			
			BusInMovementInfo busIMI = entry.getValue();
			String busCode = entry.getKey();
			String route = busIMI.getLastGPSPoint().getRoute();
			
			if (busIMI != null && busIMI.getLastGPSPoint() != null && bulmaFilter.getBusStopPoints().containsKey(route)) {
				
				int stopSequence = 1;
				
				while (bulmaFilter.getBusStopPoints().get(route).containsKey(stopSequence)) {
					List<BusStopInfo> busStopPoints = bulmaFilter.getBusStopPoints().get(route).get(stopSequence);
					
					for (int i = 0; i < busStopPoints.size(); i++) {
						BusStopInfo busStop = busStopPoints.get(i);
						
						if (bbIsBetween(busIMI, busStop)) {
							
							float headwayProgramado = 0;
							
							headwayProgramado = (i + 1) < busStopPoints.size() ? Math.abs(busStop.getSeconds() - busStopPoints.get(i + 1).getSeconds()) :	Math.abs(busStop.getSeconds() - busStopPoints.get(i - 1).getSeconds());
							
							String busOnStopTimeStamp = bbInterpolation(busIMI, busStop);
							
							BusHeadwayInfo busHeadwayInfo = new BusHeadwayInfo(route, busCode, headwayProgramado, busOnStopTimeStamp);
							
							bbBusHeadwayPopulate(route, busStop, busHeadwayInfo);
							
							bbHeadwayPopulate(route, busStop, headwayProgramado);
						}
					}
					stopSequence++;
				}
			}		
		}
	}
	
	private void bbDistanceDetection() {
		bbDistance.clear();
		
		Map<String, LinkedList<BusInMovementInfo>> groupedBusesByRoute = new HashMap<String, LinkedList<BusInMovementInfo>>();
		
		listRoutes.forEach((route) -> groupedBusesByRoute.put(route, new LinkedList<BusInMovementInfo>()));

		busInMoviment.forEach((busCode, busInMovementInfo) -> 
			groupedBusesByRoute.get(busInMovementInfo.getLastGPSPoint().getRoute()).add(busInMovementInfo));
		
		for (Entry<String, LinkedList<BusInMovementInfo>> entry : groupedBusesByRoute.entrySet()) {
			
			String route = entry.getKey();
			
			LinkedList<BusInMovementInfo> busesInRoute = entry.getValue();
			
			float nOfBusesOnRoute = busesInRoute.size();
			
			while (busesInRoute.size() > 1) {
				
				BusInMovementInfo headBus = busesInRoute.removeFirst();

				// Tamanho da rota em Km
				float lengthRoute = this.bulmaFilter.getLenghtShape(route, headBus.getLastGPSPoint().getShapeId());				
				
				//Converte para metros e divide por um quarto
				float tresholdDistance = ((lengthRoute * 1000) / nOfBusesOnRoute) / 4;
				
				for (BusInMovementInfo nextBus : busesInRoute) {
					float distance = GPSPoint.getDistanceInMeters(headBus.getLastGPSPoint(), nextBus.getLastGPSPoint());
					float time = Math.abs(headBus.getLastGPSPoint().getSeconds() - nextBus.getLastGPSPoint().getSeconds());
					
					if (distance < tresholdDistance && time < 60 && headBus.getLastGPSPoint().getShapeId().equalsIgnoreCase(nextBus.getLastGPSPoint().getShapeId())) {
						bbDistancePopulate(route, headBus, nextBus);
						bbDistanceGenerateData(headBus, nextBus, lengthRoute, nOfBusesOnRoute, tresholdDistance, distance, time);
						if (!bbBusCodeRoute.containsKey(headBus.getLastGPSPoint().getBusCode())) {
							bbBusCodeRoutePopulate(headBus.getLastGPSPoint().getBusCode(), headBus.getLastGPSPoint().getRoute());
						}
						if (!bbBusCodeRoute.containsKey(nextBus.getLastGPSPoint().getBusCode())) {
							bbBusCodeRoutePopulate(nextBus.getLastGPSPoint().getBusCode(), nextBus.getLastGPSPoint().getRoute());
						}
					}
				}
			}
		}
	}
	
	private void bbDistancePopulate(String route, BusInMovementInfo headBus, BusInMovementInfo nextBus) {
		Pairwise<GPSPoint, GPSPoint> busesInBB = Pairwise.create(headBus.getLastGPSPoint(), nextBus.getLastGPSPoint());
		
		if (!bbDistance.containsKey(route)) {
			bbDistance.put(route, new LinkedList<Pairwise<GPSPoint, GPSPoint>>());
		}
		if (!bbDistance.get(route).contains(busesInBB)) {
			bbDistance.get(route).add(busesInBB);
		} else {
			bbDistance.get(route).remove(busesInBB);
			bbDistance.get(route).add(busesInBB);
		}
	}
	
	private void bbHeadwayPopulate(String route, BusStopInfo busStop, float headwayProgramado) {
		if (bbBusHeadwayInfo.get(route).get(busStop).size() > 1) {
			BusHeadwayInfo bus1 = bbBusHeadwayInfo.get(route).get(busStop).get(0);
			BusHeadwayInfo bus2 = bbBusHeadwayInfo.get(route).get(busStop).get(1);
			
			float headwayReal = Math.abs(bus1.getSeconds() - bus2.getSeconds());
			
			if (headwayReal < (headwayProgramado / 4)) {
				
				Pairwise<BusHeadwayInfo, BusHeadwayInfo> busesInBB = Pairwise.create(bus1, bus2);
				
				if (!bbHeadway.containsKey(route)) {
					bbHeadway.put(route, new LinkedList<Pairwise<BusHeadwayInfo, BusHeadwayInfo>>());
				}
				if (!bbHeadway.get(route).contains(busesInBB)) {
					bbHeadway.get(route).add(busesInBB);
				} else {
					bbHeadway.get(route).remove(busesInBB);
					bbHeadway.get(route).add(busesInBB);
				}
				if (!bbBusCodeRoute.containsKey(bus1.getBusCode())) {
					bbBusCodeRoutePopulate(bus1.getBusCode(), bus1.getRoute());
				}
				if (!bbBusCodeRoute.containsKey(bus2.getBusCode())) {
					bbBusCodeRoutePopulate(bus2.getBusCode(), bus2.getRoute());
				}
				
				bbHeadwayGenerateData(bus1, bus2, headwayReal, headwayProgramado);
				
				bbBusHeadwayInfo.get(route).get(busStop).remove(bus1);
				bbBusHeadwayInfo.get(route).get(busStop).remove(bus2);
			}
		}
	}
	
	private void bbBusHeadwayPopulate(String route, BusStopInfo busStop, BusHeadwayInfo busHeadwayInfo) {
		if (!bbBusHeadwayInfo.containsKey(route)) {
			bbBusHeadwayInfo.put(route, new LinkedHashMap<BusStopInfo, List<BusHeadwayInfo>>());
		}
		if (!bbBusHeadwayInfo.get(route).containsKey(busStop)) {
			bbBusHeadwayInfo.get(route).put(busStop, new LinkedList<BusHeadwayInfo>());
		}
		if (!bbBusHeadwayInfo.get(route).get(busStop).contains(busHeadwayInfo)) {
			bbBusHeadwayInfo.get(route).get(busStop).add(busHeadwayInfo);
		} else {
			bbBusHeadwayInfo.get(route).get(busStop).remove(busHeadwayInfo);
			bbBusHeadwayInfo.get(route).get(busStop).add(busHeadwayInfo);
		}
	}
		
	private void bbBusStopPointsPopulate() {
		String[] rowsBusStopPoints = null;
		
		try {
			rowsBusStopPoints = ManageHDFile.openFile(getSelectedPath() + "input/stopTimeOutput.txt").split("\n");
		} catch (Exception e) {
			System.err.println("Could not read file.");
		}
		
		if (rowsBusStopPoints != null && rowsBusStopPoints.length > 0) {
			bulmaFilter.populateBusStopPoints(rowsBusStopPoints);
		}
	}
	
	private String bbInterpolation(BusInMovementInfo busIMI, BusStopInfo busSP) {
		float distanceGPSPoints = GeoPoint.getDistanceInMeters(busIMI.getPenultimateGPSPoint(), busIMI.getLastGPSPoint());
		float timeBetweenGPSPoints = Math.abs(busIMI.getPenultimateGPSPoint().getSeconds() - busIMI.getLastGPSPoint().getSeconds());
		float distanceBetweenBusSPointAndGPSPoint = GeoPoint.getDistanceInMeters(busIMI.getPenultimateGPSPoint(), busSP);

		return bbGetTimeStamp(busIMI.getPenultimateGPSPoint().getSeconds() + ((distanceBetweenBusSPointAndGPSPoint * timeBetweenGPSPoints) / distanceGPSPoints));
	}
	
	private String bbGetTimeStamp(float time) {
		int hour, minute, second;

		second = ((int) time) % 3600;
		hour = ((int) time) / 3600;
		minute = second / 60;
		second = second % 60;

		return String.format("%02d", hour) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", second);
	}
	
	private boolean bbIsBetween(BusInMovementInfo busIMI, BusStopInfo busSP) {
		
		return busIMI.getPenultimateGPSPoint() != null
			&& (GeoPoint.getDistanceInMeters(busIMI.getPenultimateGPSPoint(), busIMI.getLastGPSPoint())
					>= GeoPoint.getDistanceInMeters(busIMI.getPenultimateGPSPoint(), busSP))
			&& !busIMI.getLastGPSPoint().getLatLongString().equalsIgnoreCase(busIMI.getPenultimateGPSPoint().getLatLongString())
			&& busIMI.getLastGPSPoint().getShapeSequence() != null
			&& busIMI.getPenultimateGPSPoint().getShapeSequence() != null
			&& busSP.getClosestShapePoint() != null
			&& ((Integer.valueOf(busIMI.getPenultimateGPSPoint().getShapeSequence()) > Integer.valueOf(busIMI.getLastGPSPoint().getShapeSequence())
					&& Integer.valueOf(busIMI.getPenultimateGPSPoint().getShapeSequence()) >= Integer.valueOf(busSP.getClosestShapePoint())
					&& Integer.valueOf(busIMI.getLastGPSPoint().getShapeSequence()) <= Integer.valueOf(busSP.getClosestShapePoint()))
				|| (Integer.valueOf(busIMI.getPenultimateGPSPoint().getShapeSequence()) < Integer.valueOf(busIMI.getLastGPSPoint().getShapeSequence())
					&& Integer.valueOf(busIMI.getPenultimateGPSPoint().getShapeSequence()) <= Integer.valueOf(busSP.getClosestShapePoint())
					&& Integer.valueOf(busIMI.getLastGPSPoint().getShapeSequence()) >= Integer.valueOf(busSP.getClosestShapePoint())))
			&& busIMI.getPenultimateGPSPoint().getSeconds() < busIMI.getLastGPSPoint().getSeconds()
			&& busIMI.getLastGPSPoint().getSeconds() >= busSP.getSeconds()
			&& busIMI.getPenultimateGPSPoint().getSeconds() <= busSP.getSeconds();
	}

	private void bbHeadwayGenerateData(BusHeadwayInfo bus1, BusHeadwayInfo bus2, float headwayReal, float headwayProgramado) {
		String output =
				 bus1.getBusCode() + ","
			   + bus1.getRoute() + ","
			   + bus1.getBusOnStopTimeStamp() + ","
			   + bus1.getHeadwayProgramado() + ","
			   + "-" + ","
			   + bus2.getBusCode() + ","
			   + bus2.getRoute() + ","
			   + bus2.getBusOnStopTimeStamp() + ","
			   + bus1.getHeadwayProgramado() + ","
			   + headwayProgramado + ","
			   + headwayReal + ","
			   + (headwayProgramado / 4) + ","
			   + LINE_SEPARATOR;
		
		String file = ManageHDFile.openFile(getSelectedPath() + "/output_bb/bbDistanceOutput.csv");
	    
		if (!file.contains(output)) {
			ManageHDFile.createOutputFileBBDistance(output, false, getSelectedPath(), "Headway");
		}
	}
	
	private void bbDistanceGenerateData(BusInMovementInfo bus1, BusInMovementInfo bus2, float lengthRoute, float nOfBusesOnRoute, float tresholdDistance, float distance, float time) {
		String output =
				 bus1.getLastGPSPoint().getBusCode() + ","
			   + bus1.getLastGPSPoint().getRoute() + ","
			   + bus1.getLastGPSPoint().getShapeId() + ","
			   + bus1.getLastGPSPoint().getShapeSequence() + ","
			   + bus1.getLastGPSPoint().getSituation() + ","
			   + bus1.getLastGPSPoint().getCurrentTime() + ","
			   + bus1.getLastGPSPoint().getExpectedTime() + ","
			   + bus1.getLastGPSPoint().getLatLongString() + ","
			   + "-" + ","
			   + bus2.getLastGPSPoint().getBusCode() + ","
			   + bus2.getLastGPSPoint().getRoute() + ","
			   + bus2.getLastGPSPoint().getShapeId() + ","
			   + bus1.getLastGPSPoint().getShapeSequence() + ","
			   + bus2.getLastGPSPoint().getSituation() + ","
			   + bus2.getLastGPSPoint().getCurrentTime() + ","
			   + bus2.getLastGPSPoint().getExpectedTime() + ","
			   + bus2.getLastGPSPoint().getLatLongString() + ","
			   + lengthRoute + ","
			   + nOfBusesOnRoute + ","
			   + tresholdDistance + ","
			   + distance + ","
			   + time 
			   + LINE_SEPARATOR;
		
		String file = ManageHDFile.openFile(getSelectedPath() + "/output_bb/bbDistanceOutput.csv");
	    
		if (!file.contains(output)) {
			ManageHDFile.createOutputFileBBDistance(output, false, getSelectedPath(), "Distance");
		}
	}
	
	public void bbBusCodeRoutePopulate(String busCode, String route) {
		if (getSelectedRoute() != null && !getSelectedRoute().equals("")) {
			if (route.equals(getSelectedRoute())) {
				if (!getSelectedBusCode().equals("") && busCode.equals(getSelectedBusCode())) {
					if (!bbBusCodeRoute.containsKey(busCode)) {
						bbBusCodeRoute.put(busCode, route);
					}
				} else if (getSelectedBusCode().equals("")) {
					if (!bbBusCodeRoute.containsKey(busCode)) {
						bbBusCodeRoute.put(busCode, route);
					}
				}
			}
		} else {
			if (getSelectedBusCode() != null && !getSelectedBusCode().equals("")
					&& busCode.equals(getSelectedBusCode())) {
				if (!bbBusCodeRoute.containsKey(busCode)) {
					bbBusCodeRoute.put(busCode, route);
				}
			} else if (getSelectedBusCode() == null || getSelectedBusCode().equals("")) {
				if (!bbBusCodeRoute.containsKey(busCode)) {
					bbBusCodeRoute.put(busCode, route);
				}
			}
		}
	}
	
	// Lucas (FIM)

	private LatLng performVirtualProgress(BusInMovementInfo busIMInfo) {

		// Atualiza de acordo com GPS
		if (busIMInfo.getPenultimateGPSPoint() != null && !busIMInfo.getLastGPSPoint().isAboveThreshold()) {
			//Double velocity = getVelocity(busIMInfo.getLastGPSPoint(), busIMInfo.getPenultimateGPSPoint()) * THRESHOLD_VELOCITY;
			Double velocity = 20.0;
			return getNextClosestShapePoint(velocity, busIMInfo);
		}

		return busIMInfo.getLastGPSPoint().getLatLng();
	}

	private Double getVelocity(GPSPoint lastGPSPoint, GPSPoint penultimateGPSPoint) {
		Double distance = lastGPSPoint.getDistanceTraveled() - penultimateGPSPoint.getDistanceTraveled();
		float time = lastGPSPoint.getSeconds() - penultimateGPSPoint.getSeconds();

		if (time > 0) {
			return Math.abs(distance / time);
		}

		return 0.0;
	}

	public void onNodeSelect(NodeSelectEvent event) {
		setLastSelectedNode(event.getTreeNode());
		ProgressBarBean.setLastSelectedNode(event.getTreeNode());
	}

	public void refreshTreeFolders() {
		try {
			//folders = service.createFolders();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void filterBusesMap() {
		this.mapModel = new DefaultMapModel();
		checkNewGPSPoints();
		createLinesShapesOnMap();
		
		for (BusInMovementInfo busInMovementInfo : busInMoviment.values()) {
			createPointFiltered(busInMovementInfo.getLastGPSPoint());
		}
	}
	
	public void showOnMap() {
		
		bulmaFilter.getShapesMap().clear();
		setSelectedNode(getLastSelectedNode());
		setSelectedPath(service.getNodePath(selectedNode));
		
		if (getSelectedPath().contains("CampinaGrande")) {
			setCenterMap("-7.228448, -35.881222");
		} else if (getSelectedPath().contains("Curitiba")) {
			setCenterMap("-25.427005793164472, -49.26437591837578");
		} else if (getSelectedPath().contains("SaoPaulo")) {
			setCenterMap("-23.551260, -46.633746");
		}
		
		ManageHDFile.createOutputFileBBDistance("", true, getSelectedPath(), selectedDetection);

		populateListShapes();
		bbBusStopPointsPopulate();
		
		cleanMap();
		
		checkNewGPSPoints();

		createLinesShapesOnMap();

//		for (BusInMovementInfo busInMovementInfo : busInMoviment.values()) {
//			createPointFiltered(busInMovementInfo.getLastGPSPoint());
//		}
	}

	private void createPointFiltered(GPSPoint gpsPoint) {
		if (getSelectedRoute() != null && !getSelectedRoute().equals("")) {
			if (gpsPoint.getRoute().equals(getSelectedRoute())) {
				if (!getSelectedBusCode().equals("") && gpsPoint.getBusCode().equals(getSelectedBusCode())) {
					createPointOnMap(gpsPoint);
					setCenterMap(gpsPoint.getLatLongString());

				} else if (getSelectedBusCode().equals("")) {
					createPointOnMap(gpsPoint);
					setCenterMap(gpsPoint.getLatLongString());
				}
			}
		} else {

			if (getSelectedBusCode() != null && !getSelectedBusCode().equals("")
					&& gpsPoint.getBusCode().equals(getSelectedBusCode())) {
				createPointOnMap(gpsPoint);
				setCenterMap(gpsPoint.getLatLongString());

			} else if (getSelectedBusCode() == null || getSelectedBusCode().equals("")) {
				createPointOnMap(gpsPoint);
				setCenterMap(gpsPoint.getLatLongString());
			}
		}
	}

	private void createLinesShapesOnMap(String route) {
		String currentColor;
		for (ShapeLine shapeLine : bulmaFilter.getShapesMap().get(route)) {
			currentColor = getRandomColor();

			if (shapeLine.getColor() == null) {
				shapeLine.setColor(currentColor);
			}
			Polyline polyline = new Polyline();
			for (ShapePoint shapePoint : shapeLine.getShapePoints()) {
				// Here we have all points of each shape selected
				polyline.getPaths().add(shapePoint.getLatLng());
				polyline.setStrokeWeight(10);
				polyline.setStrokeColor(shapeLine.getColor());
				polyline.setStrokeOpacity(0.3);
			}
			mapModel.addOverlay(polyline);
		}
	}

	private void createLinesShapesOnMap() {

		if (getSelectedRoute() != null && !getSelectedRoute().equals("")) {
			createLinesShapesOnMap(getSelectedRoute());

		} else {
			String currentColor;
			for (List<ShapeLine> listShapeLines : bulmaFilter.getShapesMap().values()) {
				
				if (!listRoutes.contains(listShapeLines.get(0).getRoute())) {
					listRoutes.add(listShapeLines.get(0).getRoute());
				}
				
				// Here we have all shapes
				currentColor = getRandomColor();
				for (ShapeLine shapeLine : listShapeLines) {
					if (shapeLine.getColor() == null) {
						shapeLine.setColor(currentColor);
					}
					Polyline polyline = new Polyline();
					for (ShapePoint shapePoint : shapeLine.getShapePoints()) {
						// Here we have all points of each shape selected
						polyline.getPaths().add(shapePoint.getLatLng());
						polyline.setStrokeWeight(10);
						polyline.setStrokeColor(shapeLine.getColor());
						polyline.setStrokeOpacity(0.05);
					}
					mapModel.addOverlay(polyline);
				}
			}
		}
	}

	private String getRandomColor() {
		Random randCol = new Random();
		String color = String.format("#%06X", randCol.nextInt(0xFFFFFF + 1));
		if (color.equals("#4270d0") || color.equals("#FF0000")) {
			color = getRandomColor();
		}
		return color;
	}

	public void populateListShapes() {
		setShapePath(getSelectedPath());
		
		String[] rowsShape = null;
		try {
			rowsShape = ManageHDFile.openFile(getShapePath()).split("\n");
		} catch (Exception e) {
			System.err.println("Could not read file.");
		}

		if (rowsShape != null && rowsShape.length > 0) {
			bulmaFilter.populateListShapes(rowsShape);
		}
	}
	
	private void createPointOnMap(GPSPoint gpsPoint) {
		if (!listBusCode.contains(gpsPoint.getBusCode())) {
			listBusCode.add(gpsPoint.getBusCode());
		}

		Marker marker = new Marker(gpsPoint.getLatLng(), gpsPoint.getRoute());
		marker = setMarkerContent(marker, gpsPoint);

		mapModel.addOverlay(marker);

		setCenterMap(gpsPoint.getLatLongString());
	}

	private Marker setMarkerContent(Marker marker, GPSPoint gpsPoint) {
		marker.setTitle(gpsPoint.getBusCode());
		String stringContent = "<p>" + "Bus Code: " + gpsPoint.getBusCode() + "</p>" + "<p>" + "Route: "
				+ gpsPoint.getRoute() + "</p>" + "<p>" + "Last Update: " + gpsPoint.getCurrentTime() + "</p>" + "<p>"
				+ "Situation: " + gpsPoint.getSituation() + "</p>";

		marker.setData(stringContent);
		if (gpsPoint.isAboveThreshold()) {
			marker.setIcon(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
					+ "/resources/images/bus-red-small.png");

		} else if (gpsPoint.getSituation().equals("IN ADVANCE")) {
			marker.setIcon(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
					+ "/resources/images/bus-green-small.png");

		} else if (gpsPoint.getSituation().equals("LATE")) {
			marker.setIcon(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
					+ "/resources/images/bus-orange-small.png");
		} else {
			marker.setIcon(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
					+ "/resources/images/bus-blue-small.png");
		}
		
		if (bbDetection) {
			if (bbBusCodeRoute.containsKey(gpsPoint.getBusCode())) {
				marker.setIcon(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
						+ "/resources/images/bus-purple-small.png");
			} else {
				marker.setIcon(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
						+ "/resources/images/bus-gray-small.png");
			}
		}
		return marker;
		
	}
	
	public void checkNewGPSPoints() {
		try {
		List<String> listPathFiles = getFilesHDFS();

		for (String pathFile : listPathFiles) {
			String[] rows = ManageHDFile.openFile(pathFile).split("\n");

			for (int i = 0; i < rows.length; i++) {
				String[] fields = rows[i].split(",");

				try {
					String route = fields[0];
					Double distanceTraveled = Double.valueOf(fields[3]);
					String shapeSequence = fields[4];
					String shapeId = fields[5];
					String busCode = fields[6];
					Double latBus = Double.valueOf(fields[8]);
					Double lngBus = Double.valueOf(fields[9]);
					String problem = fields[10];
					String expectedTime = fields[12];
					String currentTime = fields[13];
					String situation = fields[14];

					GPSPoint gpsPoint = new GPSPoint(new LatLng(latBus, lngBus), problem, busCode, currentTime, shapeId,
							shapeSequence, distanceTraveled, route, expectedTime, currentTime, situation);

					GPSPoint penultimateGPSPoint = null;
					if (busInMoviment.containsKey(busCode)) {
						penultimateGPSPoint = busInMoviment.get(busCode).getLastGPSPoint();
					} else {
						createPointFiltered(gpsPoint);
					}
					newMapPoints.put(busCode, gpsPoint);

					busInMoviment.put(busCode, new BusInMovementInfo(penultimateGPSPoint, gpsPoint,
							gpsPoint.getShapeSequence(), gpsPoint.getTime()));

					if (!busCodeRoute.containsKey(busCode)) {
						busCodeRoute.put(busCode, route);
					}
					
				} catch (Exception e) {
					System.err.println("It's impossible to create GPSPoint.");
				}
			}
		}
		} catch (Exception e) {
			reportMessageError(e.getMessage());
		}
	}

	private List<String> getFilesHDFS() throws Exception {
		List<String> listPathFiles = new ArrayList<>();
		String outputPath = getSelectedPath()  + "output/";
		
		try {
			File[] jsonArrayDirectories = ManageHDFile.listFiles(outputPath);
			List<String> listToDelete = new ArrayList<>();
			
			for (int i = 0; i < jsonArrayDirectories.length; i++) {
				File directory = jsonArrayDirectories[i];
				String directoryName = directory.getName();
				long currentModificationTime = directory.lastModified();

				if (directoryName.endsWith(".GPS") && currentModificationTime > getLastModificationTime()) {
					setLastModificationTime(currentModificationTime);

					File[] jsonArrayFiles = ManageHDFile.listFiles(outputPath + directoryName);

					for (int j = 0; j < jsonArrayFiles.length; j++) {
						File file = jsonArrayFiles[j];
						String fileName = file.getName();

						if (fileName.startsWith("part")) {
							listPathFiles.add(outputPath + directoryName + "/" + fileName);
						}
					}
				} else {
					listToDelete.add(outputPath + directoryName);
				}
			}
			
			for (String pathToDelete : listToDelete) {
				ManageHDFile.deleteFile(pathToDelete);
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		return listPathFiles;
	}
	
	public void cleanMap() {
		this.mapModel = new DefaultMapModel();
		this.busInMoviment.clear();
		this.newMapPoints.clear();
		this.listRoutes.clear();
		this.listBusCode.clear();
		this.busCodeRoute.clear();
		setSelectedRoute("");
	}

	public MapModel getMapModel() {
		return mapModel;
	}

	public void onLayerSelect(OverlaySelectEvent event) {
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Circle Selected"));
		// TODO change this message.
	}

	public String getCenterMap() {
		return centerMap;
	}

	public void setCenterMap(String centerMap) {
		this.centerMap = centerMap;
	}

	public TreeNode getFolders() {
		return folders;
	}

	public void setFolders(TreeNode folders) {
		this.folders = folders;
	}

	public FileService getService() {
		return service;
	}

	public void setService(FileService service) {
		this.service = service;
	}

	public String getSelectedPath() {
		return selectedPath;
	}

	public void setSelectedPath(String selectedPath) {
		this.selectedPath = selectedPath.replace(" ", "");
	}

	public TreeNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(TreeNode selectedNode) {
		this.selectedNode = selectedNode;
	}

	public TreeNode getLastSelectedNode() {
		return lastSelectedNode;
	}

	public void setLastSelectedNode(TreeNode lastSelectedNode) {
		this.lastSelectedNode = lastSelectedNode;
	}

	public long getLastModificationTime() {
		return lastModificationTime;
	}

	public void setLastModificationTime(long lastModificationTime) {
		this.lastModificationTime = lastModificationTime;
	}

	public int getZoomMap() {
		return zoomMap;
	}

	public void setZoomMap(int zoom) {
		this.zoomMap = zoom;
	}

	public BulmaFilter getBulmaFilter() {
		return bulmaFilter;
	}

	public void setBulmaFilter(BulmaFilter bulmaFilter) {
		this.bulmaFilter = bulmaFilter;
	}

	public List<String> getListBusCode() {
		Collections.sort(listBusCode);
		return listBusCode;
	}

	public void setListBusCode(List<String> listBusCode) {
		this.listBusCode = listBusCode;
	}

	public List<String> getListRoutes() {
		Collections.sort(listRoutes);
		return listRoutes;
	}

	public void setListRoutes(List<String> listRoutes) {
		this.listRoutes = listRoutes;
	}

	public String getSelectedRoute() {
		return selectedRoute;
	}

	public void setSelectedRoute(String selectedRoute) {
		this.selectedRoute = selectedRoute;
	}

	public String getSelectedBusCode() {
		return selectedBusCode;
	}

	public void setSelectedBusCode(String selectedBusCode) {
		this.selectedBusCode = selectedBusCode;
	}

	public void setShapePath(String path) {
		String newPath = path.substring(0, path.length()-1).trim();
		this.shapePath = newPath + "/input/shape_" + newPath.substring(newPath.lastIndexOf("/")+1) + ".csv";
	}
	
	public String getShapePath() {
		return this.shapePath;
	}
		
	public String getSelectedDetection() {
		return selectedDetection;
	}

	public void setSelectedDetection(String selectedDetection) {
		bbCount = 0;
		bbBusCodeRoute.clear();
		this.selectedDetection = selectedDetection;
	}

	public boolean isBbDetection() {
		return bbDetection;
	}

	public void setBbDetection(boolean bbDetection) {
		bbCount = 0;
		bbBusCodeRoute.clear();
		this.bbDetection = bbDetection;
	}

	public Map<String, String> getBbBusCodeRoute() {
		return bbBusCodeRoute;
	}

	public void setBbBusCodeRoute(Map<String, String> bbBusCodeRoute) {
		this.bbBusCodeRoute = bbBusCodeRoute;
	}

	public Map<String, List<Pairwise<BusHeadwayInfo, BusHeadwayInfo>>> getBbHeadway() {
		return bbHeadway;
	}

	public void setBbHeadway(Map<String, List<Pairwise<BusHeadwayInfo, BusHeadwayInfo>>> bbHeadway) {
		this.bbHeadway = bbHeadway;
	}

	public Map<String, List<Pairwise<GPSPoint, GPSPoint>>> getBbDistance() {
		return bbDistance;
	}

	public void setBbDistance(Map<String, List<Pairwise<GPSPoint, GPSPoint>>> bbDistance) {
		this.bbDistance = bbDistance;
	}
}