package alice.test;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;


public class Util {

	
	public String readFileInput(String fileName){
		StringBuilder result = new StringBuilder("");

		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				result.append(sCurrentLine);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return result.toString();
	}
	
}
