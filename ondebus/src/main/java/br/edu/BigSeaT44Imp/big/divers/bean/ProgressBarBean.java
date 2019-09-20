package br.edu.BigSeaT44Imp.big.divers.bean;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.primefaces.context.RequestContext;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import br.edu.BigSeaT44Imp.big.divers.filtro.BulmaFilter;
import br.edu.BigSeaT44Imp.big.divers.model.BusProgressModel;
import br.edu.BigSeaT44Imp.big.divers.model.ShapeLine;
import br.edu.BigSeaT44Imp.big.divers.model.ShapePoint;
import br.edu.BigSeaT44Imp.big.divers.service.FileService;

@Component
@Lazy
@Scope("session")
@SessionScoped
@ManagedBean
public class ProgressBarBean extends AbstractBean implements Serializable {

	
	private static final long serialVersionUID = 1540348951661740408L;
	private List<String> listBusCode;
	private Map<String, Integer> readShapePoints;
	private Map<String, List<ShapeLine>> shapesOfTheBus;
	private Map<String, String> busesSituation;
	private List<BusProgressModel> busesList;
	private List<BusProgressModel> selectedBusesList;
	private String selectedRoute;
	private long lastModificationTime;
	private BulmaFilter bulmaFilter;
	private String selectedPath;
	private List<String> listRoutes;
	private String selectedBusCode;
	
	private static final String TYPE_DIRECTORY = "DIRECTORY";
	private String shapePath;
	private TreeNode selectedNode;
	private static TreeNode lastSelectedNode;

	@Autowired
	private FileService service;

	private int c = 0;
	
	@PostConstruct
	public void init() {

		lastModificationTime = System.currentTimeMillis() - 1;
		bulmaFilter = new BulmaFilter();

		listBusCode = new ArrayList<>();
		listRoutes = new ArrayList<>();

		this.readShapePoints = new HashMap<>();
		this.shapesOfTheBus = new HashMap<>();
		this.busesSituation = new HashMap<>();
		this.busesList = new ArrayList<>();
		this.selectedBusesList = new ArrayList<>();
		this.selectedRoute = "";
	}

	public void selectRoute() {
		listBusCode = new ArrayList<>();

		for (BusProgressModel entry : busesList) {
			if (getSelectedRoute() == null || getSelectedRoute().equals("")) {
				listBusCode.add(entry.getBusCode());

			} else if (!getSelectedRoute().equals("") && entry.getRoute().equals(getSelectedRoute())) {
				listBusCode.add(entry.getBusCode());
			}
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


	public void checkNewGPSPointsProgress() throws Exception {

		try {
			// removeOldHDFSFiles();
			List<String> listPathFiles = getFilesHDFS();

			for (String pathFile : listPathFiles) {
				String[] rows = ManageHDFile.openFile(pathFile).split("\n");

				for (int i = 0; i < rows.length; i++) {
					String[] fields = rows[i].split(",");

					if (!fields[0].equals("")) {
						String route = fields[0];
						String shapeSequence = fields[4];
						String shapeId = fields[5];
						String busCode = fields[6];
						String situation = fields[14];
						String situationColor = getSituation(situation);
						
						//TODO apenas para teste (verificar BULMART)
						if (busCode.equals("1088") || busCode.equals("3027") || busCode.equals("1066")) {
							if (10 == c ) {
								situationColor = "green";
								c++;
							} else if (c > 20) {
								situationColor = "orange";
							} else {
								situationColor = "blue";
								c++;
							}
						}

						if (!readShapePoints.containsKey(busCode)) {
							readShapePoints.put(busCode, 0);
						}
						
						busesSituation.put(busCode, situationColor);
						
						int totalReadPoints = readShapePoints.get(busCode);

						if (shapeId != null && !shapeId.isEmpty() && !shapeId.equals("-") && route != null
								&& !route.isEmpty() && !route.equals("REC")) {

							totalReadPoints = 0;
							if (!shapesOfTheBus.containsKey(busCode)) {
								shapesOfTheBus.put(busCode, new ArrayList<ShapeLine>());
							}

							if (!containsShapeLine(busCode, shapeId)) {
								ShapeLine getShape = null;

								for (ShapeLine shape : bulmaFilter.getShapesMap().get(route)) {
									if (shape.getShapeId().equals(shapeId)) {
										getShape = shape;
										break;
									}
								}

								shapesOfTheBus.get(busCode).add(getShape);
							}

							// check the position of the closest shape point
							// (where is the gps point)
							outerloop: for (ShapeLine entry : shapesOfTheBus.get(busCode)) {
								List<ShapePoint> shapePointsList = entry.getShapePoints();
								for (ShapePoint shapePoint : shapePointsList) {
									totalReadPoints++;
									if (shapePoint.getShapeSequence().equals(shapeSequence)) {
										break outerloop;
									}
								}
							}
						}

						readShapePoints.put(busCode, totalReadPoints);

						// update filtered buses in the panel
						for (int j = 0; j < selectedBusesList.size(); j++) {
							if (selectedBusesList.get(j).getBusCode().equals(busCode)) {
								selectedBusesList.get(j).setSituation(situationColor);
								selectedBusesList.get(j).setTotalReadPoints(totalReadPoints);
								selectedBusesList.get(j).setShapes(shapesOfTheBus.get(busCode));
								break;
							}
						}
					}
				}
			}

			// update all the buses
			updateBusesList();

		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	private String getSituation(String situation) {
		String color;
		
		switch (situation) {
		
			case "LATE":
				color = "orange";
				break;
	
			case "IN ADVANCE":
				color = "green";
				break;
	
			default:
				color = "blue";
				break;
		}
		
		return color;
	}

	private boolean containsShapeLine(String busCode, String shapeId) {
		boolean contains = false;
		List<ShapeLine> shapeLineList = shapesOfTheBus.get(busCode);
		for (ShapeLine shapeLine : shapeLineList) {
			if (shapeLine.getShapeId().equals(shapeId)) {
				contains = true;
				break;
			}
		}

		return contains;
	}

	private void updateBusesList() {

		for (Entry<String, List<ShapeLine>> entry : shapesOfTheBus.entrySet()) {
			String busCode = entry.getKey();
			List<ShapeLine> shapes = entry.getValue();
			Integer currentTotalReadPoints = readShapePoints.get(busCode);
			String situation = busesSituation.get(busCode);
			
			if (busesList.isEmpty()) {
				BusProgressModel busPM = new BusProgressModel(busCode, situation, shapes, currentTotalReadPoints);
				busesList.add(busPM);

			} else {
				for (int i = 0; i < busesList.size(); i++) {
					BusProgressModel bus = busesList.get(i);

					if (bus.getBusCode().equals(busCode)) {
						busesList.get(i).setSituation(situation);
						busesList.get(i).setShapes(shapes);
						busesList.get(i).setTotalReadPoints(currentTotalReadPoints);
						break;
					} else if (i == busesList.size() - 1) {
						BusProgressModel busPM = new BusProgressModel(busCode, situation, shapes, currentTotalReadPoints);
						busesList.add(busPM);
						break;
					}
				}
			}
		}
	}
	
	private void updateBusesList(String selectedBus) {
		selectedBusesList.clear();

		if (selectedBus == null || selectedBus.isEmpty()) {
			selectedBusesList = new ArrayList<BusProgressModel>(busesList);

		} else {
			if (!busesList.isEmpty()) {
				for (BusProgressModel bus : busesList) {
					if (bus.getBusCode().equals(selectedBus)) {
						selectedBusesList.add(bus);
						break;
					}
				}
			}
		}

		if (selectedBusesList.isEmpty()) {
			reportMessageInfo("No data from now. Try again in a few seconds.");
		}
	}

	private void updateRouteBusesList(String selectedRoute) {
		selectedBusesList.clear();

		if (selectedRoute == null || selectedRoute.isEmpty()) {
			selectedBusesList = new ArrayList<BusProgressModel>(busesList);

		} else {
			if (!busesList.isEmpty()) {
				for (BusProgressModel bus : busesList) {
					if (bus.getRoute().equals(selectedRoute)) {
						selectedBusesList.add(bus);
					}
				}
			}
		}

		if (selectedBusesList.isEmpty()) {
			reportMessageError("No data to display!");
		}
	}

	private void cleanData() {
		this.listRoutes.clear();
		this.listBusCode.clear();
		this.shapesOfTheBus.clear();
		this.busesList.clear();
		this.selectedBusesList.clear();
		this.bulmaFilter.getShapesMap().clear();
		setSelectedRoute("");
	}

	public void showBusProgress() {
		setSelectedNode(getLastSelectedNode());
		setSelectedPath(service.getNodePath(selectedNode));
		
		cleanData();
		populateListShapes();
		
		try {
			checkNewGPSPointsProgress();
			populateListRoutes();
			populateListBuses();
			updateBusesList("");
			startProgressBar();
		} catch (Exception e) {
			startProgressBar();
			reportMessageError(e.getMessage());
		}
	}

	private void startProgressBar() {
		if (getSelectedBusesList() == null) {
			setSelectedBusesList(new ArrayList<BusProgressModel>());
		}
		RequestContext.getCurrentInstance().addCallbackParam("buses", new Gson().toJson(getSelectedBusesList()));
	}

	public void populateListRoutes() {
		for (List<ShapeLine> listShapeLines : bulmaFilter.getShapesMap().values()) {
			if (!listRoutes.contains(listShapeLines.get(0).getRoute())) {
				listRoutes.add(listShapeLines.get(0).getRoute());
			}
		}
	}

	public void populateListBuses() {

		for (Entry<String, List<ShapeLine>> entry : shapesOfTheBus.entrySet()) {
			if (!listBusCode.contains(entry.getKey())) {
				listBusCode.add(entry.getKey());
			}
		}
	}

	public void filterBuses() {
		// display the selected bus
		if (getSelectedBusCode() != null && !getSelectedBusCode().equals("")) {
			updateBusesList(getSelectedBusCode());

			// display the buses of the selected route
		} else if (getSelectedRoute() != null && !getSelectedRoute().equals("")) {
			updateRouteBusesList(getSelectedRoute());

		} else { // display all the buses
			updateBusesList("");
		}
	}

	/*private void removeOldHDFSFiles() throws Exception {
		JSONArray jsonArrayDirectories = service.listFiles(selectedPath);

		for (int i = 0; i < jsonArrayDirectories.length(); i++) {
			JSONObject directory = jsonArrayDirectories.getJSONObject(i);
			String directoryName = directory.getString("pathSuffix");
			long currentModificationTime = directory.getLong("modificationTime");

			if (directory.getString("type").equals(TYPE_DIRECTORY) && directoryName.endsWith(".GPS")
					&& currentModificationTime <= getLastModificationTime()) {
				deleteDirectory(selectedPath + directoryName);
			}
		}
	}*/

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

	public List<BusProgressModel> getSelectedBusesList() {
		Collections.sort(selectedBusesList);
		return selectedBusesList;
	}

	public void setSelectedBusesList(List<BusProgressModel> selectedBusesList) {
		this.selectedBusesList = selectedBusesList;
	}

	public List<BusProgressModel> getBusesList() {
		return busesList;
	}

	public void setBusesList(List<BusProgressModel> busesList) {
		this.busesList = busesList;
	}

	public long getLastModificationTime() {
		return lastModificationTime;
	}

	public void setLastModificationTime(long lastModificationTime) {
		this.lastModificationTime = lastModificationTime;
	}

	public BulmaFilter getBulmaFilter() {
		return bulmaFilter;
	}

	public void setBulmaFilter(BulmaFilter bulmaFilter) {
		this.bulmaFilter = bulmaFilter;
	}

	public String getSelectedPath() {
		return selectedPath;
	}

	public void setSelectedPath(String selectedPath) {
		this.selectedPath = selectedPath.replace(" ", "");
	}

	public List<String> getListRoutes() {
		Collections.sort(listRoutes);
		return listRoutes;
	}

	public void setListRoutes(List<String> listRoutes) {
		this.listRoutes = listRoutes;
	}

	public FileService getService() {
		return service;
	}

	public void setService(FileService service) {
		this.service = service;
	}

	public String getSelectedBusCode() {
		return selectedBusCode;
	}

	public void setSelectedBusCode(String selectedBusCode) {
		this.selectedBusCode = selectedBusCode;
	}

	public List<String> getListBusCode() {
		Collections.sort(listBusCode);
		return listBusCode;
	}

	public void setListBusCode(List<String> listBusCode) {
		this.listBusCode = listBusCode;
	}

	public Map<String, Integer> getReadShapePoints() {
		return readShapePoints;
	}

	public void setReadShapePoints(Map<String, Integer> readShapePoints) {
		this.readShapePoints = readShapePoints;
	}

	public Map<String, List<ShapeLine>> getShapesOfTheBus() {
		return shapesOfTheBus;
	}

	public void setShapesOfTheBus(Map<String, List<ShapeLine>> shapesOfTheBus) {
		this.shapesOfTheBus = shapesOfTheBus;
	}

	public String getSelectedRoute() {
		return selectedRoute;
	}

	public void setSelectedRoute(String selectedRoute) {
		this.selectedRoute = selectedRoute;
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

	public static void setLastSelectedNode(TreeNode node) {
		lastSelectedNode = node;
	}

	public Map<String, String> getBusesSituation() {
		return busesSituation;
	}

	public void setBusesSituation(Map<String, String> busesSituation) {
		this.busesSituation = busesSituation;
	}
	
	public void setShapePath(String path) {
		String newPath = path.substring(0, path.length()-1).trim();
		this.shapePath = newPath + "/input/shape_" + newPath.substring(newPath.lastIndexOf("/")+1) + ".csv";
	}
	
	public String getShapePath() {
		return this.shapePath;
	}
}