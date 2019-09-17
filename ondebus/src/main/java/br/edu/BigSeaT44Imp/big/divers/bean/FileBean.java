package br.edu.BigSeaT44Imp.big.divers.bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.map.Circle;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Polyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import br.edu.BigSeaT44Imp.big.divers.filtro.BulmaFilter;
import br.edu.BigSeaT44Imp.big.divers.model.Bus;
import br.edu.BigSeaT44Imp.big.divers.model.FileRow;
import br.edu.BigSeaT44Imp.big.divers.model.GPSPoint;
import br.edu.BigSeaT44Imp.big.divers.model.ShapeLine;
import br.edu.BigSeaT44Imp.big.divers.model.ShapePoint;
import br.edu.BigSeaT44Imp.big.divers.service.FileService;

@Component
@Lazy
@ViewScoped
@ManagedBean
public class FileBean extends AbstractBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private TreeNode files;
	private TreeNode folders;
	private String name;
	private String path;
	private String nameDirectory;
	private String newName;
	private String pathDelete;
	private TreeNode selectedNode;
	private String selectedPath;
	private List<FileRow> fileRows;
	private Integer resultLength;
	
	private BulmaFilter bulmaFilter;	
	private MapModel mapModel;
	private String centerMap;

	@Autowired
	private FileService service;

	@PostConstruct
	public void init() {
		refreshTrees();
		RequestContext.getCurrentInstance().execute("jQuery('#pnlContentBulma').hide()");
		setCenterMap("-7.228448, -35.881222");
	}

	public void refreshTrees() {
		refreshTreeFiles();
		refreshTreeFolders();
	}
	
	public void refreshTreeFiles() {
		try {
			files = service.createFiles();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void submitJob() {
		refreshTreeFiles();

		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("jQuery('#pnlContentFile').hide()");
		context.execute("jQuery('#pnlContentBulma').hide()");
		context.execute("jQuery('#pnlContentJar').show()");
	}

	public void refreshTreeFolders() {
		try {
			folders = service.createFolders();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onSelectContextMenu(NodeSelectEvent event) {
		try {
			setSelectedNode(event.getTreeNode());
			setSelectedPath(service.getNodePath(selectedNode));
		} catch (Exception e) {
		}
	}

	public void onNodeSelect(NodeSelectEvent event) {
		setSelectedNode(event.getTreeNode());
		setSelectedPath(service.getNodePath(selectedNode));
		
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("jQuery('#pnlContentJar').hide()");
		context.execute("jQuery('#pnlContentBulma').hide()");
		
		try {
			fileRows = service.getRowsFile(getSelectedPath().substring(1));
			resultLength = getFileRows().size();
			context.execute("jQuery('#pnlContentFile').show()");
			
		} catch (JSONException e) {
			context.execute("jQuery('#pnlContentFile').hide()");
			reportMessageError("File is too big to be opened by DEMaaS.");
		}
	}

	public void deleteDirectory() {
		JSONObject jsonEntity = service.delete("deleteDirectory", selectedPath.substring(1, selectedPath.length() - 1));
		String data = jsonEntity.get("data").toString();
		JSONObject dataJson = new JSONObject(data);

		try {
			if (jsonEntity.get("code").toString().equals("200") && (boolean) dataJson.get("boolean")) {
				reportMessageSuccessful(jsonEntity.get("mesg").toString());
				refreshTrees();
			} else {
				reportMessageError(jsonEntity.get("mesg").toString());
			}
		} catch (Exception e) {
			reportMessageError("Unexpected error!");
		}
	}

	public void uploadFile(FileUploadEvent event) {

		if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
			event.setPhaseId(PhaseId.INVOKE_APPLICATION);
			event.queue();
		} else {
			String pathDst = getSelectedPath().substring(1) + event.getFile().getFileName();
			UploadedFile file = event.getFile();
			JSONObject jsonEntity;
			try {

				if (file.getSize() != 0) {
					jsonEntity = service.uploadFile("uploadFile", file, pathDst);

					if (jsonEntity.get("code").toString().equals("201")) {
						reportMessageSuccessful(jsonEntity.get("mesg").toString());
						refreshTrees();

					} else {
						reportMessageError(
								jsonEntity.get("error").toString() + "\n " + jsonEntity.get("mesg").toString());
					}
				} else {
					reportMessageError("It is not possible to send empty file.");
				}

			} catch (IOException e) {
				reportMessageError("Failed : IOException error code : " + e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
				reportMessageError("Unexpected error!");
			}
		}
	}

	public void downloadFile() {
		String hadoopPath = getSelectedPath().substring(1, getSelectedPath().length() - 1);
		String nameFile = hadoopPath.substring(hadoopPath.lastIndexOf("/"));
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		String home = System.getProperty("user.home");
		params.add("destinationPath", home + "/Downloads/" + nameFile);
		params.add("hadoopPath", hadoopPath);
		JSONObject jsonEntity = service.get("downloadFile", params);

		String error = null;
		try {
			error = jsonEntity.get("error").toString();
		} catch (JSONException e) {
		}
		if (error == null) {
			reportMessageSuccessful(jsonEntity.get("mesg").toString());
		} else {
			reportMessageError(jsonEntity.get("error").toString());
		}
	}

	public void deleteFile() {
		JSONObject jsonEntity = service.delete("deleteFile", selectedPath.substring(1, selectedPath.length() - 1));
		String data = jsonEntity.get("data").toString();
		JSONObject dataJson = new JSONObject(data);
		try {
			if (jsonEntity.get("code").toString().equals("200") && (boolean) dataJson.get("boolean")) {
				reportMessageSuccessful(jsonEntity.get("mesg").toString());
				refreshTreeFiles();
			} else {
				reportMessageError(jsonEntity.get("error").toString());
			}
		} catch (Exception e) {
			reportMessageError("Unexpected error!");
		}
	}

	public void renameFile() {
		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		String newPath = service.getPathRename(selectedPath.substring(1));
		formData.add("currentPath", selectedPath.substring(1));
		formData.add("newPath", newPath + newName);

		JSONObject jsonEntity = service.put("renameFile", formData, MediaType.APPLICATION_FORM_URLENCODED_TYPE);

		String error = null;
		try {
			error = jsonEntity.get("error").toString();
		} catch (JSONException e) {
		}

		if (error == null) {
			reportMessageSuccessful(jsonEntity.get("mesg").toString());
			refreshTrees();
		} else {
			reportMessageError(jsonEntity.get("error").toString());
		}
	}

	public void setSelectedPathToRoot() {
		setSelectedPath("/");
	}

	public void createDirectory() {
		if (nameDirectory.trim().equals("")) {
			reportMessageError("Name can not be empty.");
		} else {
			JSONObject jsonEntity = service.put("createDirectory", selectedPath.substring(1) + nameDirectory,
					MediaType.TEXT_PLAIN);

			String error = null;
			try {
				error = jsonEntity.get("error").toString();
			} catch (JSONException e) {
			}

			if (error == null) {
				reportMessageSuccessful(jsonEntity.get("mesg").toString());
				refreshTrees();
			} else {
				reportMessageError(jsonEntity.get("error").toString() + "\n " + jsonEntity.get("mesg").toString());
			}
		}
	}

	public void showMapPanel() {

		cleanMap();

		String gpsPath = getSelectedPath().substring(1, getSelectedPath().length() - 1);
		String[] rowsGPS = service.openFile(gpsPath).split("\n");

		if (rowsGPS.length == 0 || !rowsGPS[0].contains(BulmaFilter.HEADER_OUTPUT_BULMA)) {
			reportMessageError("This file isn't a output of BULMA");

			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("jQuery('#pnlContentFile').hide()");
			context.execute("jQuery('#pnlContentJar').hide()");
			context.execute("jQuery('#pnlContentBulma').hide()");
		
		} else {
			bulmaFilter.populateListGPS(rowsGPS);

			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("jQuery('#pnlContentFile').hide()");
			context.execute("jQuery('#pnlContentJar').hide()");
			context.execute("jQuery('#pnlContentBulma').show()");
		}
	}
	
	public void onShapeFileSelected(NodeSelectEvent event) {
		setSelectedNode(event.getTreeNode());

		String shapePath = service.getNodePath(selectedNode);
		shapePath = shapePath.substring(1, shapePath.length() - 1);

		String[] rowsShape = null;
		try {
			rowsShape = service.openFile(shapePath).split("\n");
		} catch (Exception e) {
			System.err.println("Could not read file.");
		}

		if (rowsShape == null || rowsShape.length == 0 || !rowsShape[0].contains(BulmaFilter.HEADER_SHAPE_FILE)) {
			reportMessageError("This file isn't a shape file or it isn't on the required format.");
			bulmaFilter.setShapeFilePath("");

		} else {
			bulmaFilter.setShapeFilePath(shapePath);
			bulmaFilter.populateListShapes(rowsShape);
		}
	}
	
	public void populateMap() {
		mapModel = new DefaultMapModel();
		Bus selectedBus = bulmaFilter.getSelectedBus();

		List<ShapeLine> shapesRouteSelected = bulmaFilter.getShapesMap().get(selectedBus.getRoute());

		if (shapesRouteSelected == null) {
			reportMessageError("This file doesn't have the same route as the selected gps."); 

		} else {

			for (ShapeLine shapeLine : shapesRouteSelected) {
				// Here we have all shapes of the route of the bus selected
				Polyline polyline = new Polyline();
				for (ShapePoint shapePoint : shapeLine.getShapePoints()) {
					// Here we have all points of each shape selected
					polyline.getPaths().add(shapePoint.getLatLng());
					polyline.setStrokeWeight(10);
					polyline.setStrokeColor("#FF9900");
					polyline.setStrokeOpacity(0.3);
				}
				mapModel.addOverlay(polyline);
			}

			if (bulmaFilter.getSelectedTrip() == null || bulmaFilter.getSelectedTrip().equals("")) {
				List<GPSPoint> listAllPoints = new LinkedList<>();
				for (Entry<String, List<GPSPoint>> entry : selectedBus.getMapTrips().entrySet()) {
					// Here we have all trips
					listAllPoints.addAll(entry.getValue());
				}
				
				createPointsTrip(listAllPoints);

			} else {
				createPointsTrip(selectedBus.getMapTrips().get(bulmaFilter.getSelectedTrip()));
			}
		}
	}
	
	private void createPointOnMap(GPSPoint gpsPoint) {
		Circle circle = null;

		if (gpsPoint.hasProblem()) {
			circle = new Circle(gpsPoint.getLatLng(), 5);
			circle.setStrokeColor("#FF0000");
			circle.setFillColor("#FF0000");

		} else {
			circle = new Circle(gpsPoint.getLatLng(), 5);
			circle.setStrokeColor("#4270d0");
			circle.setFillColor("#4270d0");

		}

		circle.setFillOpacity(0.8);
		mapModel.addOverlay(circle);

		// TODO melhorar esse método. O centro não fica mto bom no mapa
		setCenterMap(gpsPoint.getLatLongString());
	}
	
	public void cleanMap() {
		mapModel = new DefaultMapModel();
		bulmaFilter = new BulmaFilter();
	}
	
	private void createPointsTrip(List<GPSPoint> listGPSPoints) {

		Integer initialIndex = null;
		Integer finalIndex = null;
		try {
			initialIndex = Integer.valueOf(bulmaFilter.getInitialPoint()) - 1 ;
		} catch (NullPointerException e) {
			initialIndex = 0;
		}
		
		try {
			finalIndex = Integer.valueOf(bulmaFilter.getFinalPoint());
		} catch (NullPointerException e) {
			finalIndex = listGPSPoints.size();
		}
		
		for (int i = initialIndex; i < finalIndex; i++) {
			// Here we have all points of each trip
			createPointOnMap( listGPSPoints.get(i));
		}
	}
	
	public MapModel getMapModel() {
		return mapModel;
	}

	public void onLayerSelect(OverlaySelectEvent event) {
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Circle Selected"));
		// TODO change this message.
	}
	
	public Integer getResultLength() {
		return resultLength;
	}

	public void setResultLength(Integer resultLength) {
		this.resultLength = resultLength;
	}

	public List<FileRow> getFileRows() {
		return fileRows;
	}

	public void setFileRows(List<FileRow> fileRows) {
		this.fileRows = fileRows;
	}

	public String getPathDelete() {
		return pathDelete;
	}

	public void setPathDelete(String pathDelete) {
		this.pathDelete = pathDelete;
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public String getNameDirectory() {
		return nameDirectory;
	}

	public void setNameDirectory(String nameDirectory) {
		this.nameDirectory = nameDirectory;
	}

	public String getSelectedPath() {
		return selectedPath;
	}

	public void setSelectedPath(String selectedPath) {
		this.selectedPath = selectedPath;
	}

	public TreeNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(TreeNode selectedNode) {
		this.selectedNode = selectedNode;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setService(FileService service) {
		this.service = service;
	}

	public TreeNode getFiles() {
		return files;
	}

	public void setFiles(TreeNode files) {
		this.files = files;
	}

	public TreeNode getFolders() {
		return folders;
	}

	public void setFolders(TreeNode folders) {
		this.folders = folders;
	}

	public String getCenterMap() {
		return centerMap;
	}

	public void setCenterMap(String centerMap) {
		this.centerMap = centerMap;
	}

	public BulmaFilter getBulmaFilter() {
		return bulmaFilter;
	}

	public void setBulmaFilter(BulmaFilter bulmaFilter) {
		this.bulmaFilter = bulmaFilter;
	}
}