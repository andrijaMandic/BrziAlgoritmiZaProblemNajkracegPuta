package spp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * Klasa koja sadrzi metode koje pomazu usporeddbi promatranih algoritama.
 * Metoda doTesting mogla bi se pokretati i iz main funkcije, no testovi su 
 * pokretani iz testnog programa u /test folderu.
 * 
 * @author mandic
 */
public class TestingAlgorithms {
    
    /**
     * Metoda za usporedbu algoritama najkraceg puta. 
     * Metoda prima instancu klase Algorithm kako bi bila upotrebljiva
     * za CH, ALT i Dijkstrin algoritam.
     * 
     * @param sppAlgorithm
     * @param numIterations
     * @param numNodes
     * @param fileName
     * @param algoName
     * @throws Exception 
     */
    public static void doTesting(Algorithm sppAlgorithm, int numIterations, int numNodes, String fileName, String algoName) throws Exception{
                
        String param = "";
        if(algoName.equals("ALT"))
            param += "16";
        if(algoName.equals("CH"))
            param += "f";
        
        String testFileName = fileName + "-" + numIterations;
        BufferedReader br = new BufferedReader(new FileReader("data//test//" + testFileName + ".txt"));
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("data//results//NY//ALT//"  + testFileName + "-" + param + "tmp.txt"))); //+ algoName + "//"
        
        int oldEdges = sppAlgorithm.getNumberOfEdges();
        
        
        long preprocessingTime = 0;
        
        if((sppAlgorithm instanceof LandmarkAlgorithm) || (sppAlgorithm instanceof ContractionHierarchies)){
            //System.out.println("Pokrecem predprocesiranje.");
            long startTime = System.currentTimeMillis();
            sppAlgorithm.preprocess(16, "random");
            long endTime = System.currentTimeMillis();
            preprocessingTime = endTime - startTime;
            //System.out.println("Gotovo predprocesiranje.");
        }
        //System.out.println("Edges: " + oldEdges);
        //System.out.println("Added edges: " + (sppAlgorithm.getNumberOfEdges() - oldEdges));
        
        
        String line = br.readLine();
        ArrayList<Integer> startNode = new ArrayList<>(numIterations);
        ArrayList<Integer> endNode = new ArrayList<>(numIterations);
        ArrayList<Integer> distance = new ArrayList<>(numIterations);
        for(int i = 0; i < numIterations; ++i){
            line = br.readLine();
            StringTokenizer st = new StringTokenizer(line);
            
            startNode.add(Integer.parseInt(st.nextToken()));
            endNode.add(Integer.parseInt(st.nextToken()));
            distance.add(Integer.parseInt(st.nextToken()));
        }
        br.close();
            
        ArrayList<Integer> spLengths = new ArrayList<>(numIterations);
        ArrayList<Integer> numSettledNodes = new ArrayList<>(numIterations);
        //System.out.println("Krece faza upita.");
        long startTime = System.currentTimeMillis();
        for(int i = 0; i < numIterations; ++i){
            int spLen = sppAlgorithm.computeShortestPath(startNode.get(i), endNode.get(i));
            //Ukoliko izmjenimo ulazni parametar u DijkstrasAlgoritam. Za pokretane dvosmjerne pretrage.
            //int spLen = sppAlgorithm.bidirectionalSearch(startNode.get(i), endNode.get(i));
            int numSetled = sppAlgorithm.getNumberOfSettledNodes();            
            spLengths.add(spLen);
            numSettledNodes.add(numSetled);
        }
        long endTime = System.currentTimeMillis();
        
        int wrongAns = 0;
        //Provjera tocnosti rjesenja.
        for(int i = 0; i < numIterations; ++i){
            if(!spLengths.get(i).equals(distance.get(i))){
                throw new Exception("Wrong shortest path! " + spLengths.get(i) + "!=" + distance.get(i));
                //System.out.println("Wrong shortest path! " + spLengths.get(i) + "!=" + distance.get(i));
                //wrongAns++;
            }
        }
        //System.out.println("Ukupno krivo " + wrongAns + " upita.");
        
        double lenAvg = 0.0;
        double settledAvg = 0.0;
        int minLen = Integer.MAX_VALUE;
        int maxLen = Integer.MIN_VALUE;
        int minSettled = Integer.MAX_VALUE;
        int maxSettled = Integer.MIN_VALUE;
        for(int i = 0; i < numIterations; ++i){
            int len = spLengths.get(i);
            int settled = numSettledNodes.get(i);
            
            lenAvg += ((double)len / numIterations);
            settledAvg += ((double)settled / numIterations);
            
            if(minLen > len) minLen = len;
            if(maxLen < len) maxLen = len;
            if(minSettled > settled) minSettled = settled;
            if(maxSettled < settled) maxSettled = settled;
        }
        out.println("Data: " + fileName);
        out.println("Nodes: " + sppAlgorithm.getNumberOfNodes());
        //System.out.println("Nodes: " + sppAlgorithm.getNumberOfNodes());
        out.println("Edges: " + oldEdges);
        
        out.println(algoName + " algorithm\n");
        out.println("Iterations: " + numIterations);
        out.println("Preprocessing time: " + preprocessingTime + " ms = " + (double)preprocessingTime / 1_000 + " s");
        out.println("Added edges: " + (sppAlgorithm.getNumberOfEdges() - oldEdges));
        out.println("Query time: " + (endTime - startTime) + " ms = " + (double)(endTime - startTime)/1_000 + " s");
        out.println("\nAverage query time: " + (double)(endTime - startTime)/numIterations + " ms");
        out.println("Average SP time cost: " + lenAvg + " ms = " + String.format("%.2f", (lenAvg / 60_000)) + " min");
        out.println("Average number of settled nodes: " + String.format("%.2f", settledAvg));
        out.println("\nMin length: " + minLen + " ms = " + String.format("%.2f", ((double)(minLen) / 60_000)) + " min");
        out.println("Max length: " + maxLen + " ms = " + String.format("%.2f", ((double)(maxLen) / 60_000)) + " min");
        out.println("Max settled: " + maxSettled);
        out.println("Min settled: " + minSettled);
        out.close();
    }
    
    /**
     * Metoda koja daje informacije o tocnosti heuristike ALT algoritma.
     * 
     * @param alt
     * @param fileName
     * @param option
     * @param landmarks
     * @throws IOException 
     */
    public static void estimateDistALT(LandmarkAlgorithm alt, String fileName, String option, int ... landmarks) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader("data//test//" + fileName + "-1000.txt"));
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("data//results//NY//ALT//"  + fileName + "-" + option + "-avgCorr.txt"))); //+ algoName + "//"
        
        String line = br.readLine();
        ArrayList<Integer> startNode = new ArrayList<>(1000);
        ArrayList<Integer> endNode = new ArrayList<>(1000);
        ArrayList<Integer> distance = new ArrayList<>(1000);
        for(int i = 0; i < 1000; ++i){
            line = br.readLine();
            StringTokenizer st = new StringTokenizer(line);

            startNode.add(Integer.parseInt(st.nextToken()));
            endNode.add(Integer.parseInt(st.nextToken()));
            distance.add(Integer.parseInt(st.nextToken()));
        }
        br.close();
        
        out.println(fileName);
        
        for(int numLandmarks : landmarks){
            System.out.println("Na redu je k = " + numLandmarks);
            alt.preprocess(numLandmarks, option);
            

            ArrayList<Integer> spEstimations = new ArrayList<>(1000);
            for(int i = 0; i < 1000; ++i){
                int spLen = alt.calculateHeuristicFunction(startNode.get(i), endNode.get(i));
                spEstimations.add(spLen);
            }

            double avgCorr = 0.0;
            for(int i = 0; i < 1000; ++i){
                int len = spEstimations.get(i);
                int dist = distance.get(i);

                avgCorr += ((double)(len) / dist);
                if(len > dist)
                    throw new IOException("Estimation cannot be higher than real SP.");
            }
            out.println( option + ", landmarks = " + numLandmarks + ", avg correct = " + String.format("%.4f",(avgCorr / 1000)));
        }
        out.close();
    }
    
    public static void main(String[] args) throws Exception {
        
    }
}
