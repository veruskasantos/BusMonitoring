package br.edu.BigSeaT44Imp.big.divers.service;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import br.edu.BigSeaT44Imp.big.divers.model.FileNode;
import br.edu.BigSeaT44Imp.big.divers.model.FileRow;

@Service
@Component
public class FileService extends AbstractService {

	private final String MANAGER_FILE_PATH = getApplicationPath() + "/rest/managerFiles/";
	private static final String TYPE_DIRECTORY = "DIRECTORY";

	public FileService() {
		super();
		setManagerPath(MANAGER_FILE_PATH);
	}

	
	public String getJSONData(JSONObject jsonEntity, String title) throws Exception {
		String data = null;
		try {
			data = jsonEntity.getString("data");
		} catch (JSONException e) {
			throw new Exception("No data found.");
		}
		return data;
	}
	
	public JSONArray listFiles(String path) throws Exception {
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add("path", path);
		JSONObject jsonEntity = super.get("listFiles", params);
		
		String data = getJSONData(jsonEntity, "data");
		JSONObject jsonData = new JSONObject(data);

		JSONObject jsonFileStatuses = jsonData.getJSONObject("FileStatuses");
		JSONArray jsonArray = jsonFileStatuses.getJSONArray("FileStatus");
		return jsonArray;
	}
	
	public void createFiles(TreeNode parent, String path, int childrenNum) throws Exception {

		if (childrenNum > 0) {
			
			JSONArray jsonArrayFiles = listFiles(path);

			for (int i = 0; i < jsonArrayFiles.length(); i++) {
				JSONObject file = jsonArrayFiles.getJSONObject(i);

				if (file.getString("type").equals(TYPE_DIRECTORY)) {
					TreeNode node = new DefaultTreeNode(
							new FileNode(file.getString("pathSuffix"), "-", file.getString("type")), parent);
					createFiles(node, path + file.getString("pathSuffix") + "/", file.getInt("childrenNum"));

				} else {
					new DefaultTreeNode("document",
							new FileNode(file.getString("pathSuffix"), "-", file.getString("type")), parent);
				}
			}
		}
	}

	public void createFolders(TreeNode parent, String path, int childrenNum) throws Exception {

		if (childrenNum > 0) {

			JSONArray jsonArrayFiles = listFiles(path);

			for (int i = 0; i < jsonArrayFiles.length(); i++) {
				JSONObject file = jsonArrayFiles.getJSONObject(i);

				if (file.getString("type").equals(TYPE_DIRECTORY)) {
					TreeNode node = new DefaultTreeNode(
							new FileNode(file.getString("pathSuffix"), "-", file.getString("type")), parent);
					createFolders(node, path + file.getString("pathSuffix") + "/", file.getInt("childrenNum"));
				} 
			}
		}
	}
	
	public TreeNode createFolders() throws Exception {
		TreeNode root = new DefaultTreeNode(new FileNode("", "-", TYPE_DIRECTORY), null);
		createFolders(root, "", 1);
		return root;
	}
	
	public TreeNode createFiles() throws Exception {
		TreeNode root = new DefaultTreeNode(new FileNode("", "-", TYPE_DIRECTORY), null);
		createFiles(root, "", 1);
		return root;
	}
		
	public String getNodePath(TreeNode treeNode) {
		List<String> namesNodes = new ArrayList<String>();
		if (treeNode != null) {
			namesNodes.add(treeNode.toString());
			treeNode = treeNode.getParent();
			while (treeNode != null) {
				namesNodes.add(0, treeNode.toString());
				treeNode = treeNode.getParent();
			}
		}

		String output = "";
		for (String nameNode : namesNodes) {
			output += nameNode + "/";
		}
		return output;
	}

	public String getPathRename(String path) {
		for (int i = path.length() - 2; i >= 0; i--) {
			if (i == 0) {
				path = "";
				break;
				
			} else if (path.substring(i, i+1).equals("/")) {
				path = path.substring(0, i+1);
				break;
			}
		}
		return path;
	}
	
	public List<FileRow> getRowsFile(String path) {
		List<FileRow> rowsList = new ArrayList<FileRow>();
		String[] rows =  openFile(path).split("[\n]");
		for (int i = 0; i < rows.length; i ++) {
			rowsList.add(new FileRow(rows[i]));
		}
		
		return rowsList;
	}
	
	public String openFile(String path) {
		MultivaluedMap<String, String> param = new MultivaluedMapImpl();
		param.add("path", path);
		JSONObject jsonEntity = get("openFile", param);
		String result = null;
		
		result = jsonEntity.getString("result");
		
		return result;
	}
}