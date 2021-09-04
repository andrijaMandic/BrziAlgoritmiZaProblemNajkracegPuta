package spp;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Klasa jedinicnih testova Dijkstrinog algoritma.
 * 
 * @author mandic
 */
public class DijkstrasAlgorithmTest {
    
    public DijkstrasAlgorithmTest() {
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
     * Test of computeShortestPath method, of class DijkstrasAlgorithm.
     */
    @Test
    public void testComputeShortestPathSimple() {
        System.out.println("computeShortestPath simple test");
        
        RoadNetwork graph = new RoadNetwork("simpleGraphTest");
        DijkstrasAlgorithm instance = new DijkstrasAlgorithm(graph);
        
        assertEquals(0, instance.computeShortestPath(1, 1));
        assertEquals(400, instance.computeShortestPath(0, 1));
        assertEquals(100, instance.computeShortestPath(2, 1));
        assertEquals(Integer.MAX_VALUE, instance.computeShortestPath(2, 0));
        assertEquals(Integer.MAX_VALUE, instance.computeShortestPath(1, 0));
    }
    
    @Test
    public void testComputeShortestPathComplex(){
        System.out.println("computeShortestPath complex test");
        
        RoadNetwork graph = new RoadNetwork("kusalicGraphTest");
        DijkstrasAlgorithm instance = new DijkstrasAlgorithm(graph);
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
    
    /**
     * Test of computeShortestPathMultipleSources method, of class DijkstrasAlgorithm.
     */
    @Test
    public void testComputeShortestPathMultipleSources(){
        System.out.println("computeShortestPathMultipleSources test");
        
        RoadNetwork graph = new RoadNetwork("kusalicGraphTest");
        DijkstrasAlgorithm instance = new DijkstrasAlgorithm(graph);
        
        ArrayList<Integer> sources = new ArrayList<>();
        sources.add(0);
        sources.add(7);
        assertEquals(7, instance.computeShortestPathMultipleSources(sources, 2));
        assertEquals(14, instance.computeShortestPathMultipleSources(sources, 8));
        assertEquals(4, instance.computeShortestPathMultipleSources(sources, 3));
        
        sources.clear();
        sources.add(1);
        sources.add(2);
        assertEquals(10, instance.computeShortestPathMultipleSources(sources, 6));
    }
    
    /**
     * Test of bidirectionalSearch method, of class DijkstrasAlgorithm.
     */
    @Test
    public void testBidirectionalSearch(){
        System.out.println("bidirectionalSearch test");
        
        RoadNetwork graph = new RoadNetwork("kusalicGraphTest");
        DijkstrasAlgorithm instance = new DijkstrasAlgorithm(graph);
        assertEquals(0, instance.bidirectionalSearch(0, 0));
        assertEquals(10, instance.bidirectionalSearch(0, 1));
        assertEquals(13, instance.bidirectionalSearch(0, 2));
        assertEquals(4, instance.bidirectionalSearch(0, 3));
        assertEquals(12, instance.bidirectionalSearch(0, 4));
        
        assertEquals(10, instance.bidirectionalSearch(5, 0));
        assertEquals(6, instance.bidirectionalSearch(6, 0));
        assertEquals(9, instance.bidirectionalSearch(7, 0));
        assertEquals(20, instance.bidirectionalSearch(8, 0));
    }
    
     /**
     * Test of getCalculatedShortestpath method, of class DijkstrasAlgorithm.
     */
    @Test
    public void testGetCalculatedShorthestPath(){
        System.out.println("getCalculatedShorthestPath test");
        
        RoadNetwork graph = new RoadNetwork("kusalicGraphTest");
        DijkstrasAlgorithm instance = new DijkstrasAlgorithm(graph);
        
        instance.computeShortestPath(0, 0);
        assertEquals("[0]", instance.getCalculatedShorthestPath(0));
        
        instance.computeShortestPath(0, 1);
        assertEquals("[0,1]", instance.getCalculatedShorthestPath(1));
        
        instance.computeShortestPath(0, 4);
        assertEquals("[0,3,6,7,4]", instance.getCalculatedShorthestPath(4));
        
        instance.computeShortestPath(5, 0);
        assertEquals("[5,7,6,3,0]", instance.getCalculatedShorthestPath(0));
    }
}
