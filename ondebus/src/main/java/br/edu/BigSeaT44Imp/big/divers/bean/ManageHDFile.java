package br.edu.BigSeaT44Imp.big.divers.bean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ManageHDFile {

	private static final String LINE_SEPARATOR = "\n";
//	private static final String userPath = "C:/Users/Lucas/workspace/BusMonitoring/data";
	private static final String userPath = "/home/veruska/Documentos/Mestrado/BusMonitoring/data";
//	private static final String userPath = "/home/andreza/Documents/BusMonitoring/data";

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
		String file = "";
		try {

			file = new String(Files.readAllBytes(Paths.get(userPath + path)), StandardCharsets.UTF_8);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return file;
	}
	
	// Lucas (INICIO)
	
	public static void createOutputFileBBDistance(String output, boolean sobrescrever, String path, String detection) {		
		if (sobrescrever) {
			try {
				String city = path.substring(path.indexOf("/"));
				FileWriter file = new FileWriter(userPath + city + "/output_bb/bbDistanceOutput.csv");			
				String headerOutput;
				
				if (detection.equalsIgnoreCase("Distance")) {
					headerOutput = "BUSCODE,ROUTE,SHAPEID,SHAPESEQUENCE,SITUATION,CURRENTTIME,EXPECTEDTIME,LAT,LONG,-,BUSCODE,ROUTE,SHAPEID,SHAPESEQUENCE,SITUATION,CURRENTTIME,EXPECTEDTIME,LAT,LONG,ROUTESIZE,nBUSES,TRESHOLD,DISTANCEAB,TIMEBETWEENBUSES" + LINE_SEPARATOR;
				} else {
					headerOutput = "BUSCODE,ROUTE,BUSONSTOPTIMESTAMP,HEADWYPROGRAMADO,-,BUSCODE,ROUTE,BUSONSTOPTIMESTAMP,HEADWYPROGRAMADO,HEADWAYPROGRAMADO,HEADWAYREAL,TRESHOLD" + LINE_SEPARATOR;
				}
				
				file.write(headerOutput);
				file.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				FileWriter file = new FileWriter(userPath + path + "/input/saida.txt", true);			
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
