package br.edu.BigSeaT44Imp.big.divers.bean;

import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.json.JSONArray;
import org.json.JSONObject;
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
import br.edu.BigSeaT44Imp.big.divers.model.BusInMovementInfo;
import br.edu.BigSeaT44Imp.big.divers.model.FileNode;
import br.edu.BigSeaT44Imp.big.divers.model.GPSPoint;
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

	// bus_code, last_gps_position
	private Map<String, BusInMovementInfo> busInMoviment;
	private Map<String, String> busCodeRoute;

	@Autowired
	private FileService service;
	private Integer count;

	//
	private static String[] CITIES_NAME = {"Campina Grande", "Curitiba"};
	private static String BULMA_RT_PATH = "BULMA_RT/";
	private static float THRESHOLD_VELOCITY = 0.6f;
	private String shapePath;
	
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

		listBusCode = new ArrayList<>();
		listRoutes = new ArrayList<>();

		populateCitiesNameList();
	}
	
	public void onPageLoad() {
		RequestContext.getCurrentInstance().addCallbackParam("newMarkers", new Gson().toJson(""));
		cleanMap();
	}

	public void populateCitiesNameList() {
		
		TreeNode root = new DefaultTreeNode(new FileNode("", "-", TYPE_DIRECTORY), null);
		
		for (String city : CITIES_NAME) {
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

	private LatLng getClosestShapePoint(Double velocity, BusInMovementInfo busIMInfo) {
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
						if (distanceOnCurrentTime >= currentDeltaDistance) {
							busIMInfo.setShapeSequence(shapeSequence);
							// TODO Mudar a linha abaixo
//							busIMInfo.getLastGPSPoint().setShapeSequence(shapeSequence);
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

			RequestContext.getCurrentInstance().addCallbackParam("newMarkers", new Gson().toJson(newMarkers));
		} else {
			RequestContext.getCurrentInstance().addCallbackParam("newMarkers", new Gson().toJson(""));
		}

	}

	private LatLng performVirtualProgress(BusInMovementInfo busIMInfo) {

		// Atualiza de acordo com GPS
		if (busIMInfo.getPenultimateGPSPoint() != null && !busIMInfo.getLastGPSPoint().isAboveThreshold()) {
			Double velocity = getVelocity(busIMInfo.getLastGPSPoint(), busIMInfo.getPenultimateGPSPoint()) * THRESHOLD_VELOCITY;
			return getClosestShapePoint(velocity, busIMInfo);
		}

		return busIMInfo.getLastGPSPoint().getLatLng();
	}

	private Double getVelocity(GPSPoint lastGPSPoint, GPSPoint penultimateGPSPoint) {
		Double distance = lastGPSPoint.getDistanceTraveled() - penultimateGPSPoint.getDistanceTraveled();
		long time = lastGPSPoint.getTime() - penultimateGPSPoint.getTime();

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
		setSelectedPath(service.getNodePath(selectedNode).replaceFirst("/", ""));
		
		if (getSelectedPath().contains("CampinaGrande")) {
			setCenterMap("-7.228448, -35.881222");
		} else if (getSelectedPath().contains("Curitiba")) {
			setCenterMap("-25.427005793164472, -49.26437591837578");
		} else if (getSelectedPath().contains("SaoPaulo")) {
			setCenterMap("-23.551260, -46.633746");
		}
		
		populateListShapes();
		
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
		this.selectedPath = BULMA_RT_PATH + selectedPath.replace(" ", "");
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
}