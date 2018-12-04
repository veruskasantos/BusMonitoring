package br.edu.BigSeaT44Imp.big.divers.bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class ManageHDFile {

	private static final String LINE_SEPARATOR = "\n";
	private static final String userPath = "C:/Users/Andreza/Desktop/fetech/";

	public static File[] listFiles(String path) throws Exception {

		File[] files = new File(userPath + path).listFiles();
		return files;
	}
	
	
	public static void deleteFile(String path) {
		
		try {
			File[] files = listFiles(path);
			for (int j = 0; j < files.length; j++) {
				File file = files[j];
				file.delete();
			}
			File file = new File(userPath + path);
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	// read file
	public static String openFile(String path) {

		BufferedReader br;
		String currentLine;
		String file = "";

		try {

			br = new BufferedReader(new FileReader(userPath + path));
			while ((currentLine = br.readLine()) != null) {
				file += currentLine + LINE_SEPARATOR;
			}

			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return file;
	}
}
