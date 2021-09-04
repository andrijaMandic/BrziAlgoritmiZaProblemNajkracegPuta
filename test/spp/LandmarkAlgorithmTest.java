/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spp;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *  Klasa koja sadrzi jedinicne testove za ALT algoritam.
 * 
 * @author mandi
 */
public class LandmarkAlgorithmTest {
    
    public LandmarkAlgorithmTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of selectLandmarks method, of class LandmarkAlgorithm.
     */
    @Test
    public void testSelectLandmarks() {
        System.out.println("selectLandmarks test");
        int numLandmarks = 3;
        String option = "random";
        RoadNetwork graph = new RoadNetwork("kusalicGraphTest");
        LandmarkAlgorithm instance = new LandmarkAlgorithm(graph);
        
        assertEquals(0, instance.getNumberOfLandmarks());
        instance.selectLandmarks(numLandmarks, option);
        assertEquals(3, instance.getNumberOfLandmarks());
        instance.selectLandmarks(2*numLandmarks, option);
        assertEquals(6, instance.getNumberOfLandmarks());
        
        option = "farthest";
        instance.selectLandmarks(numLandmarks, option);
        assertEquals(3, instance.getNumberOfLandmarks());
        instance.selectLandmarks(2*numLandmarks, option);
        assertEquals(6, instance.getNumberOfLandmarks());
    }

    /**
     * Test of precomputeLandmarkDistances method, of class LandmarkAlgorithm.
     * Provjerava u sustini pretragu unazad kod Dijkstrinog algoritma, posto se na taj
     * nacin odreduju udaljenosti do vrha orijentira.
     */
    @Test
    public void testPrecomputeLandmarkDistances() {
        System.out.println("precomputeLandmarkDistances test");
        
        int numLandmarks = 3;
        String option = "random";
        RoadNetwork graph = new RoadNetwork("kusalicGraphTest");
        LandmarkAlgorithm instance = new LandmarkAlgorithm(graph);
        instance.selectLandmarks(numLandmarks, option);
        
        //Test za simetricni graf kao sto je to slucaj za kusalicGraphTest.
        for(int l = 0; l < instance.distancesFromLandmark.size(); ++l){
            for(int i = 0; i < graph.getNumNodes(); ++i)
                assertEquals(instance.distancesFromLandmark.get(l).get(i), instance.distancesToLandmark.get(l).get(i));
        }
    }
    
    /**
     * Test of od computeShortestPath
     */
    @Test
    public void testComputeShortestPath(){
        System.out.println("computeShortestPath test");
        
        RoadNetwork graph = new RoadNetwork("kusalicGraphTest");
        LandmarkAlgorithm instance = new LandmarkAlgorithm(graph);
        
        instance.preprocess(3, "random");
        assertEquals(3, instance.getNumberOfLandmarks());
        
        assertEquals(0, instance.computeShortestPath(0, 0));
        assertEquals(10, instance.computeShortestPath(0, 1));
        assertEquals(13, instance.computeShortestPath(0, 2));
        assertEquals(4, instance.computeShortestPath(0, 3));
        assertEquals(12, instance.computeShortestPath(0, 4));
        
        assertEquals(10, instance.computeShortestPath(5, 0));
        assertEquals(6, instance.computeShortestPath(6, 0));
        assertEquals(9, instance.computeShortestPath(7, 0));
        assertEquals(20, instance.computeShortestPath(8, 0));
    }
    
}
