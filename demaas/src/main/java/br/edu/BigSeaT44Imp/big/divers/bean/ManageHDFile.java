package br.edu.BigSeaT44Imp.big.divers.bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ManageHDFile {

	private static final String LINE_SEPARATOR = "\n";
	private static final String userPath = "C:/Users/Lucas/Desktop/tcc/";

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
	
	// Lucas (INICIO)
	
	public static void createOutputFileBBDistance(String output, boolean sobrescrever) {		
		if (sobrescrever) {
			try {
				FileWriter file = new FileWriter("C:/Users/Lucas/Desktop/tcc/BULMA_RT/CampinaGrande/input/saida.txt");			
				String headerOutput = "BUSCODE,ROUTE,SHAPEID,SHAPESEQUENCE,SITUATION,CURRENTTIME,EXPECTEDTIME,LAT,LONG,-,BUSCODE,ROUTE,SHAPEID,SHAPESEQUENCE,SITUATION,CURRENTTIME,EXPECTEDTIME,LAT,LONG,ROUTESIZE,nBUSES,TRESHOLD,DISTANCEAB,TIMEBETWEENBUSES" + LINE_SEPARATOR;
				file.write(headerOutput);
				file.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				FileWriter file = new FileWriter("C:/Users/Lucas/Desktop/tcc/BULMA_RT/CampinaGrande/input/saida.txt", true);			
				file.write(output);
				file.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	// Lucas (FIM)
}
