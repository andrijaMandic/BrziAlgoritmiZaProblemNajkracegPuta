package spp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Klasa koja implementira ALT algoritam.
 * 
 * @author mandic
 */
public class LandmarkAlgorithm extends Algorithm{
    private RoadNetwork graph;
    private DijkstrasAlgorithm dijkstra;
    private ArrayList<Integer> landmarksIds;
    protected ArrayList<ArrayList<Integer>> distancesToLandmark;
    protected ArrayList<ArrayList<Integer>> distancesFromLandmark;
    
    public LandmarkAlgorithm(RoadNetwork graph){
        this.graph = graph;
        dijkstra = new DijkstrasAlgorithm(graph);
        
        landmarksIds = new ArrayList<>();
        distancesToLandmark = new ArrayList<>();
        distancesFromLandmark = new ArrayList<>();
    }
    
    /**
     * Metoda koja provodi odabir vrhova orijentacije. 
     * Potrebno je odabrati slucajan odabir - random, ili pohlepni onajudaljeniji 
     * odabir - farthest.
     * 
     * @param numLandmarks
     * @param option 
     */
    public void selectLandmarks(int numLandmarks, String option){
        landmarksIds.clear();
        if(option.equals("random")){
            //Slucajni odabir vrhova.
            HashSet<Integer> selected = new HashSet<>(numLandmarks);
            while(selected.size() != numLandmarks){
                Random rnd = new Random();
                int newLandmark = rnd.nextInt(graph.getNumNodes());
                if(!selected.contains(newLandmark)){
                    selected.add(newLandmark);
                    landmarksIds.add(newLandmark);
                }
            }
        }
        else if(option.equals("farthest")){
            //Pohlepni najudaljeniji odabir vrhova.
            Random rnd = new Random();
            int rndFirst = rnd.nextInt(graph.getNumNodes());
            dijkstra.computeShortestPath(rndFirst, -1);
            //Pronalazimo najudaljeniji vrh od slucajno odabranog vrha.
            //Takav vrh ce biti prvi odabrani landmark. Pretpostavlja se 
            //kako je numLandmarks >= 1.
            int firstLandmark = -1;
            int maxDist = Integer.MIN_VALUE;
            for(int i = 0; i < graph.getNumNodes(); ++i){
                if(dijkstra.dist.get(i) > maxDist){
                    maxDist = dijkstra.dist.get(i);
                    firstLandmark = i;
                }
            }
            landmarksIds.add(firstLandmark);
            for(int k = 0; k < numLandmarks - 1; ++k){
                //Pronalazimo Dijkstra pretrazivanjem najudaljeniji vrh od do sad odabranih orijentira.
                dijkstra.computeShortestPathMultipleSources(landmarksIds, -1);
                int newLandmark = -1;
                maxDist = Integer.MIN_VALUE;
                for(int i = 0; i < graph.getNumNodes(); ++i){
                    if(dijkstra.dist.get(i) > maxDist){
                        maxDist = dijkstra.dist.get(i);
                        newLandmark = i;
                    }
                }
                landmarksIds.add(newLandmark);
            }
        }
    }
    
    /**
     * Metoda koja racuna i sprema za svaki vrh orijentir udaljenosti od i do
     * svih preostalih vrhova grafa.
     */
    public void precomputeLandmarkDistances(){
        distancesToLandmark.clear();
        distancesFromLandmark.clear();
        for(int l = 0; l < landmarksIds.size(); ++l){
            distancesToLandmark.add(new ArrayList<>(graph.getNumNodes()));
            distancesFromLandmark.add(new ArrayList<>(graph.getNumNodes()));
            
            //Izracunamo dist(l,v), za sve vrhove v.
            dijkstra.unsetReverseSearch();
            dijkstra.computeShortestPath(landmarksIds.get(l), -1);
            for(int i = 0; i < graph.getNumNodes(); ++i)
                distancesFromLandmark.get(l).add( dijkstra.dist.get(i) );
        
            //Izracunamo dist(v,l), za sve vrhove v.
            dijkstra.setReverseSearch();
            dijkstra.computeShortestPath(landmarksIds.get(l), -1);
            for(int i = 0; i < graph.getNumNodes(); ++i)
              distancesToLandmark.get(l).add( dijkstra.dist.get(i) );
        }
    }
    
    /**
     * Metoda racunanja vrijednosti najkraceg puta ALT algoritma.
     * 
     * @param sourceNodeId
     * @param targetNodeId
     * @return vrijednost najkraceg puta za zadani par vrhova grafa.
     */
    @Override
    public int computeShortestPath(int sourceNodeId, int targetNodeId){
        
        dijkstra.setHeuristicSearch();
        return dijkstra.computeShortestPath(sourceNodeId, targetNodeId);
    }
    
    /**
     * Racuna h_targetNodeId(nodeId) - procjenu udaljenosti do ciljnog vrha.
     * @param nodeId
     * @param targetNodeId
     * @return procjena udaljenosti do ciljnog vrha
     */
    public int calculateHeuristicFunction(int nodeId, int targetNodeId){
            int maxHeuristic = Integer.MIN_VALUE;
            for(int l = 0; l < landmarksIds.size(); ++l){
                //value1 = dist(l,t) - dist(l,u), where l is landmark, t is target and u is graph node - nodeId
                int value1 = distancesFromLandmark.get(l).get(targetNodeId) - distancesFromLandmark.get(l).get(nodeId);
            
                //value2 = dist(u,l) - dist(t,l), where l is landmark, t is target and u is graph node - nodeId
                int value2 = distancesToLandmark.get(l).get(nodeId) - distancesToLandmark.get(l).get(targetNodeId);
                
                if(Math.max(value1, value2) > maxHeuristic)
                    maxHeuristic = Math.max(value1, value2);
            }
            return maxHeuristic;
    }
    
    /**
     * Metoda koja provodi fazu predprocesiranja ALT algoritma.
     * 
     * @param numLandmarks
     * @param option - "random" ili "farthest"
     */
    @Override
    public void preprocess(int numLandmarks, String option){
        selectLandmarks(numLandmarks, option);
        precomputeLandmarkDistances();
        dijkstra.setLandmarkAlgorithm(this);
    }
    
    public int getNumberOfLandmarks(){
        return landmarksIds.size();
    }
    
    @Override
    public int getNumberOfSettledNodes(){
        return dijkstra.getNumberOfSettledNodes();
    }
    
    @Override
    public int getNumberOfNodes(){
        return dijkstra.getNumberOfNodes();
    }
    
    @Override
    public int getNumberOfEdges(){
        return dijkstra.getNumberOfEdges();
    }
    
    public void clearPreprocessedData(){
        landmarksIds.clear();
        distancesFromLandmark.clear();
        distancesToLandmark.clear();
    }
}
