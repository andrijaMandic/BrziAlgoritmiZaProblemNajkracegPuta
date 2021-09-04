package spp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.StringTokenizer;

import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Klasa koja pruza metode za lakse obavljanje tehnickih stvari popt rada s obradom 
 * podataka te kreiranjem grafova kod usporedbe algoritama.
 * 
 * @author mandic
 */
public class Generator {
    
    /**
     * Metoda koja na slucajan nacin odabire parove vrhova za pronalazak najkraceg puta.
     * Uz to, Dijkstrinim pretrazivanjem racuna vrijednost najkraceg puta te sve zapisuje u 
     * zaseban file koji ce sluziti kod testiranja algoritama.
     * 
     * @param sampleSize
     * @param fileName
     * @throws IOException 
     */
    public static void generateTestData(int sampleSize, String fileName) throws IOException{
        RoadNetwork graph = new RoadNetwork(fileName);
        DijkstrasAlgorithm dijkstra = new DijkstrasAlgorithm(graph);
        int numberOfNodes = graph.getNumNodes();
        
        PrintWriter brw = new PrintWriter(new BufferedWriter(new FileWriter("data//test//" + fileName + "-" + sampleSize + ".txt")));
        Random rnd = new Random();
        
        brw.println("c startNodeId endNodeId distance(Dijkstra)");
        for(int i = 0; i < sampleSize; ++i){
            int startNode = rnd.nextInt(numberOfNodes);
            int endNode = rnd.nextInt(numberOfNodes);
            int distance = dijkstra.computeShortestPath(startNode, endNode);
            brw.println("" + startNode + " " + endNode + " " + distance);
        }
        brw.close();
    }
    
    /**
     * Metoda koja na modificiran nacin kreira parove vrhova za fazu upita algoritama.
     * Koristi se BFS pretrazivanje kako bi vrhovi bili udaljeni za odredeni broj bridova.
     * 
     * @param sampleSize
     * @param fileName
     * @throws IOException 
     */
    public static void generateTestDataBFS(int sampleSize, String fileName) throws IOException{
        RoadNetwork graph = new RoadNetwork(fileName);
        DijkstrasAlgorithm dijkstra = new DijkstrasAlgorithm(graph);
        int numberOfNodes = graph.getNumNodes();
        
        PrintWriter brw = new PrintWriter(new BufferedWriter(new FileWriter("data//test//" + fileName + "-" + sampleSize + "_BFS.txt")));
        Random rnd = new Random();
        //Udaljenost do ciljnog vrha, mjereno u broju koraka tj. prijedenih vrhova.
        int k = 700;
        
        brw.println("c startNodeId endNodeId distance(Dijkstra)");
        for(int i = 0; i < sampleSize; ++i){
            int startNode = rnd.nextInt(numberOfNodes);
            
            HashSet<Integer> visitedNodes = new HashSet<>();
            Queue<Integer> queue = new LinkedList<>();
            queue.add(startNode);
            visitedNodes.add(startNode);
            int endNode = -1;
            
            for(int dist = 0; dist < k && !queue.isEmpty(); ++dist){
                int qSize = queue.size();
                for(int j = 0; j < qSize; ++j){
                    int vertex = queue.remove();
                    for(Arc arc : graph.outgoingArcs.get(vertex)){
                        int outNode = arc.anotherEndId;
                        if(!visitedNodes.contains(outNode)){
                            visitedNodes.add(outNode);
                            queue.add(outNode);
                        }
                    }
                    if(queue.isEmpty()){
                        endNode = vertex;
                        break;
                    }
                }
            }
            //U queue strukturi se nalaze vrhovi udaljeni od pocetnog za k koraka.
            if(!queue.isEmpty()){
                ArrayList<Integer> candidates = new ArrayList<>(queue.size());
                while(!queue.isEmpty())
                    candidates.add(queue.remove());
                int index = rnd.nextInt(candidates.size());

                endNode = candidates.get(index);
            }
            int distance = dijkstra.computeShortestPath(startNode, endNode);
            brw.println("" + startNode + " " + endNode + " " + distance);
        }
        brw.close();
    }
    
    /**
     * Metoda za specificnu obradu podataka cestovnih mreza.
     * Koristi se za uskladivanje prikaza podataka iz kojih se ucitava graf.
     * 
     * @param fileName
     * @throws IOException 
     */
    public static void parseData(String fileName) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader("data//" + fileName + ".txt"));
        
        PrintWriter outCo = new PrintWriter(new BufferedWriter(new FileWriter("data//" + fileName + "-co.txt")));
        PrintWriter outGr = new PrintWriter(new BufferedWriter(new FileWriter("data//" + fileName + "-gr.txt")));
        
        String line = br.readLine();
        StringTokenizer st = new StringTokenizer(br.readLine());
        for(int i = 0; i < 3; ++i)
            line = st.nextToken();
        //Broj vrhova.
        int N = Integer.parseInt(line);
        
        //Zapisivanje sadrzaja u filename-co.txt datoteku.
        outCo.println("c Created graph - " + fileName);
        outCo.println("c Coordinates are not important in this example.");
        outCo.println("p smth sp co " + N);
        for(int i = 1; i <= N; ++i)
            outCo.println("v " + i + " 0 0");
        outCo.close();
        //Zavrsetak file *-co.txt gdje se nalazi popis vrhova.
        
        //Koristit ce nam kod generiranja tezine brida.
        Random rnd = new Random();
        //Citanje bridova iz filename datoteke te zapisivanje u prikladnom formatu u *-gr.txt datoteku.
        outGr.println("c Created graph - " + fileName);
        line = br.readLine();
        while(line != null){
            st = new StringTokenizer(line);
            int first = Integer.parseInt(st.nextToken());
            int second = Integer.parseInt(st.nextToken());
            //Ovisi o podacima, nekad izmjeniti na >=.
            if(first <= second)
                throw new IOException("Ocekivan je neusmjeren graf.");
            //Zadani graf je netezinski, no uzet cemo proizvoljnu nenegativnu tezinu brida.
            int cost = rnd.nextInt(10_000);
            outGr.println("a " + first + " " + second + " " + cost);
            outGr.println("a " + second + " " + first + " " + cost);
            
            line = br.readLine();
        }
        outGr.close();
        
        br.close();
    }
    
    /**
     * Metoda koja za specifican ispis CH algoritma kreira nove fileove
     * s potrebnim podacima za izradu grafova.
     * 
     * @param fileName
     * @throws IOException 
     */
    public static void parseOutputDataCH(String fileName) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader("data//results//COL//CH//prioritiesComparation//" + fileName + ".txt"));
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("data//results//COL//CH//prioritiesComparation//" + fileName + "_edg.txt")));
        PrintWriter outAvgD = new PrintWriter(new BufferedWriter(new FileWriter("data//results//COL//CH//prioritiesComparation//" + fileName + "_deg.txt")));
        
        String line;
        StringTokenizer st = new StringTokenizer(br.readLine());
        st.nextToken();
        
        //Podaci
        String dataName = st.nextToken();
        st = new StringTokenizer(br.readLine());
        st.nextToken();
        
        //Broj vrhova
        int N = Integer.parseInt(st.nextToken());
        st = new StringTokenizer(br.readLine());
        st.nextToken();
        
        //Broj bridova
        int E = Integer.parseInt(st.nextToken());
        out.println(dataName + " " + N + " " + E);
        outAvgD.println(dataName + " " + N + " " + E);
        
        br.readLine();
        line = br.readLine();
        out.println(line);
        outAvgD.println(line);
        
        line = br.readLine();
        while(true){
            if(line == null)
                continue;
            st = new StringTokenizer(line);
            String first = "";
            if(st.hasMoreTokens())
                first = st.nextToken();
            if(first.equals("Provedena")){
                st.nextToken(); 
                st.nextToken(); 
                int x = Integer.parseInt(st.nextToken());
                
                line = br.readLine();
                st = new StringTokenizer(line);
                st.nextToken();
                st.nextToken();
                int y = Integer.parseInt(st.nextToken());
                out.println(x + " " + y);
                
                line = br.readLine();
                st = new StringTokenizer(line);
                st.nextToken(); st.nextToken(); st.nextToken(); st.nextToken();
                String tmpS = st.nextToken(); //Micemo , s kraja
                double avgD = Double.parseDouble(tmpS.substring(0, tmpS.length() - 1));
                outAvgD.println(x + " " + avgD);
            }
            if(first.equals("Edges:")){
                st = new StringTokenizer(br.readLine());
                st.nextToken();
                st.nextToken();
                int addedEdges = Integer.parseInt(st.nextToken());
                out.println(N + " " + (E + addedEdges));
                break;
            }
            line = br.readLine();
        }
        
        out.close();
        outAvgD.close();
        br.close();
        
    }
    
    /**
     * Metoda koja na temelju podatka CH algoritma kreira graf prosjecnog stupnja vrha u 
     * ovisnosti o broju kontrahiranih vrhova.
     * 
     * @param title
     * @param fileNames
     * @throws IOException 
     */
    public static void createAvgDegreeGraph(String title, String... fileNames) throws IOException{
        XYSeriesCollection podaci = new XYSeriesCollection();
        for(String fileName : fileNames){
            BufferedReader br = new BufferedReader(new FileReader("data//results//COL//CH//avgDegree//" + fileName + ".txt"));
            
            String line = br.readLine();
            StringTokenizer st = new StringTokenizer(line);
            st.nextToken();
            int N = Integer.parseInt(st.nextToken());
            int E = Integer.parseInt(st.nextToken());
            String name = br.readLine();
            
            XYSeries series = new XYSeries(name);
            line = br.readLine();
            while(line != null){
                st = new StringTokenizer(line);
                int x = Integer.parseInt(st.nextToken());
                double y = Double.parseDouble(st.nextToken()); 
                series.add(x, y);
                line = br.readLine();
            }
            podaci.addSeries(series);
        }
        JFreeChart chart = ChartFactory.createXYLineChart("", "Kontrahirano vrhova", "Prosječni stupanj vrha", podaci, PlotOrientation.VERTICAL, true, true, false);
        ChartUtils.saveChartAsPNG(new File("data//results//COL//CH//avgDegree//" + title + ".png"), chart, 550, 250);
    }
    
    /**
     * Metoda koja za obradene izlazne podatke CH algoritma kreira graf usporedbe
     * broja dodanih vrhova precaca u ovisnosti o broju kontrahiranih vrhova.
     * 
     * @param title
     * @param fileNames
     * @throws IOException 
     */
    public static void createTotalEdgesGraph(String title, String... fileNames) throws IOException{
        XYSeriesCollection podaci = new XYSeriesCollection();
        for(String fileName : fileNames){
            BufferedReader br = new BufferedReader(new FileReader("data//results//COL//CH//prioritiesComparation//" + fileName + ".txt"));
            
            String line = br.readLine();
            StringTokenizer st = new StringTokenizer(line);
            st.nextToken();
            int N = Integer.parseInt(st.nextToken());
            int E = Integer.parseInt(st.nextToken());
            String name = br.readLine();
            
            XYSeries series = new XYSeries(name);
            line = br.readLine();
            while(line != null){
                st = new StringTokenizer(line);
                int x = Integer.parseInt(st.nextToken());
                int y = Integer.parseInt(st.nextToken());
                series.add(x, y);
                line = br.readLine();
            }
            podaci.addSeries(series);
        }
        JFreeChart chart = ChartFactory.createXYLineChart("", "Kontrahirano vrhova", "Ukupno bridova", podaci, PlotOrientation.VERTICAL, true, true, false);
        ChartUtils.saveChartAsPNG(new File("data//results//COL//CH//prioritiesComparation//" + title + ".png"), chart, 550, 350);
    }
    
    /**
     * Metoda za dobivanje naknadnih osnovnih informacija o testnom skupu koji sadrzi parove vrhova za
     * pronalazak najkraceg puta. Spremaju se podaci poput prosjecne, minimalne i maksimalne vrijednosti najkraceg puta.
     * 
     * @param fileNames
     * @throws IOException 
     */
    public static void getDataTestInfo(String... fileNames) throws IOException{
        for(String fileName : fileNames){
            BufferedReader br = new BufferedReader(new FileReader("data//test//" + fileName + ".txt"));
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("data//test//info//" + fileName + "-info.txt")));
            
            String dataName = fileName.substring(0, fileName.lastIndexOf('-'));
            RoadNetwork graph = new RoadNetwork(dataName);
            DijkstrasAlgorithm dijkstra = new DijkstrasAlgorithm(graph);
            
            int iteration = 1000; //broj testnih upita
            int minCost = Integer.MAX_VALUE;
            int maxCost = Integer.MIN_VALUE;
            int minNodesSP = Integer.MAX_VALUE;
            int maxNodesSP = Integer.MIN_VALUE;
            double avgNodesSP = 0;
            double avgCost = 0;
            
            br.readLine();
            String line = br.readLine();
            while(line != null){
                StringTokenizer st = new StringTokenizer(line);
                int s = Integer.parseInt(st.nextToken());
                int t = Integer.parseInt(st.nextToken());
                int cost = Integer.parseInt(st.nextToken());
                
                if(cost != dijkstra.computeShortestPath(s, t))
                    throw new IOException("Greska kod provjere testnih podataka.");
                
                int nodesSP = dijkstra.nodesOnShortestPath(t);
                if(minNodesSP > nodesSP)
                    minNodesSP = nodesSP;
                if(maxNodesSP < nodesSP)
                    maxNodesSP = nodesSP;
                avgNodesSP += ((double)nodesSP)/iteration;
                
                if(minCost > cost)
                    minCost = cost;
                if(maxCost < cost)
                    maxCost = cost;
                avgCost += ((double)cost)/iteration;
                
                line = br.readLine();
            }
            
            out.println("Data: " + dataName);
            out.println("\nMin nodes: " + minNodesSP);
            out.println("Max nodes: " + maxNodesSP);
            out.println("Avg nodes: " + String.format("%.2f", (avgNodesSP)));
            out.println("\nMin length: " + String.format("%.2f", ((double)minCost)/60_000) + " min");
            out.println("Max length: " + String.format("%.2f", ((double)maxCost)/60_000) + " min");
            out.println("Avg length: " + String.format("%.2f", (avgCost)/60_000) + " min");
            
            out.close();
        }
    }
    
    public static void main(String[] args) throws IOException{
        /*
        Po potrebi modificirati main funkciju za dobivnje zeljenih datoteka/grafova.
        */
    }
}
