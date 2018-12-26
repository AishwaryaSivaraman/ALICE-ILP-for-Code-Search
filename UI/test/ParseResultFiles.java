package alice.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

//class exptContents {
//    public String exptFileName;
//    public List listOfTrials = new ArrayList<trial>();
//}
//
//class trial {
//    public List listOfIterations = new ArrayList<iteration>();
//}
//
//class iteration {
//    public int numExamples;
//    public String pos;
//    public String neg;
//}
public class ParseResultFiles {
    private static String inputFileName = "/home/whirlwind/Desktop/ALICE_EVAL/ExampleLog/TopDown/ExampleLogData.txt";
    private static Path inputFilePath = Paths.get(inputFileName);
    private HashMap<Integer, List<Integer>> listOfNumberOfExamples = new HashMap();
    public HashMap<String, exptContents> listOfExpts = new HashMap(); // Key is filename,Value is all contents of the specific file

    public void run() {
        try {
            System.out.println("Program starting.");
            try (BufferedReader br = new BufferedReader(new FileReader(inputFileName))) {
                String line = "none";
                do {
                    if(!line.contains("whirlwind")) line = br.readLine();
                    if(line == null) break;
                    if(line.contains("whirlwind")) { // we encounter a new file

                        exptContents e;

                        // line is "/home/whirlwind/workspace/ALICE_UI/resources/OLD_WIN3213515/SetAntiAlias.txt"

                        // extract just "SetAntiAlias.txt"
                        String justFileName = Paths.get(line).getFileName().toString();

                        // check if this key exists already
                        if(listOfExpts.containsKey(justFileName)) { // retrieve existing class from hashmap and append new data to it
                            e = listOfExpts.get(justFileName);
                        } else { // else make a new class for this file
                            e = new exptContents();
                            e.exptFileName = justFileName;
                        }

                        // now e contains either existing contents of SetAntiAlias.txt, or is a new SetAntiAlias.txt object

                        line = br.readLine(); // Start Trial
                        trial t = new trial();
                        iteration i;
                        // this contains start of trial
                        do {

                            if(!line.contains("Start Iter")) line = br.readLine();

                            // line now has Start Iter
                            i = new iteration();
                            line = br.readLine(); // Num examples
                            i.numExamples = Integer.parseInt(line.substring(line.lastIndexOf(' ')).trim());


                            line = br.readLine(); // pos
                            i.pos = line.split(",");

                            line = br.readLine(); // neg
                            i.neg = line.split(",");

                            line = br.readLine(); // end iter

                            t.listOfIterations.add(i);
                            line = br.readLine(); // we don't know what this is. could be start iter, or end trial.

                            if(line.contains("End Trial")) break;

                        } while(true);
                        e.listOfTrials.add(t);
                        listOfExpts.put(justFileName, e);
                    }


                } while(true);
            }
            System.out.println("Number of files is is " + listOfExpts.keySet().size());
        } catch(Exception e) {
            System.out.println("Exception caught " + e.toString());
        }

    }
    public void populateIteration() {

        for(String key : listOfExpts.keySet()) {
            exptContents e = listOfExpts.get(key);
            for(int i = 0; i < e.listOfTrials.size(); i++) {
                trial t = (trial)(e.listOfTrials.get(i));
                for(int j = 0; j < t.listOfIterations.size(); j++) {
                    iteration it = (iteration) (t.listOfIterations.get(j));
                    List<Integer> temp;
                    if(listOfNumberOfExamples.containsKey(Integer.valueOf(j))) {
                         temp = listOfNumberOfExamples.get(Integer.valueOf(j));
                    } else {
                        temp = new ArrayList<Integer>();
                    }
                    temp.add(Integer.valueOf(it.numExamples));
                    listOfNumberOfExamples.put(Integer.valueOf(j), temp);
                }
            }
        }
    }
    public void printAll() {
        for(String key : listOfExpts.keySet()) {
            exptContents e = listOfExpts.get(key);
            System.out.println("");
            System.out.println("Filename is " + key);
            for(int i = 0; i < e.listOfTrials.size(); i++) {
                System.out.println();
                trial t = (trial)(e.listOfTrials.get(i));
                for(int j = 0; j < t.listOfIterations.size(); j++) {
                    System.out.println("Trial " + i + " and Iteration " + j);
                    iteration it = (iteration) (t.listOfIterations.get(j));
                    System.out.println("Number of examples is " + it.numExamples);
                }
            }
        }
    }
    public void printNumExamples() {
        for(Integer key : listOfNumberOfExamples.keySet()) {
            List<Integer> temp = listOfNumberOfExamples.get(key);
            System.out.println("\nIteration " + key);
            Collections.sort(temp);

            OptionalDouble average = temp
                    .stream()
                    .mapToDouble(a -> a)
                    .average();
            Integer median = temp.get(temp.size() / 2);

            System.out.println("Average is " + average.getAsDouble() + " and median is " + median + " and Max is : " + temp.get(temp.size() - 1) + " and Min is " + temp.get(0));
        }
    }
    public static void main(String[] args) {
    	ParseResultFiles m = new ParseResultFiles();
	    m.run();
	    //m.printAll();
	    m.populateIteration();
	    m.printNumExamples();
    }
}
