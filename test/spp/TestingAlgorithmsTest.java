package spp;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Klasa koja sadrzi testove algoritama.
 * Pokrecu se algoritmi CH, ALT i Dijkstrin algoritam na raznim skupovima podataka.
 * 
 * @author mandic
 */
public class TestingAlgorithmsTest {
    
    public TestingAlgorithmsTest() {
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

    /*
    @Test
    public void testDoTesting1() throws Exception {
        System.out.println("doTesting1 Dijkstra 1000");
        RoadNetwork graph = new RoadNetwork("USA-road-CAL");
        DijkstrasAlgorithm dijkstra = new DijkstrasAlgorithm(graph);
    
        TestingAlgorithms.doTesting(dijkstra, 1000, graph.getNumNodes(), "USA-road-CAL", "Dijkstra");
    }
    */
    /*
    @Test
    public void testDoTesting2() throws Exception {
        System.out.println("doTesting2 Dijkstra 1000");
        RoadNetwork graph = new RoadNetwork("USA-road-COL");
        DijkstrasAlgorithm dijkstra = new DijkstrasAlgorithm(graph);
    
        TestingAlgorithms.doTesting(dijkstra, 1000, graph.getNumNodes(), "USA-road-COL", "Dijkstra");
    }
    */
    /*
    @Test
    public void testDoTesting3() throws Exception {
        System.out.println("doTesting3 Dijkstra 1000");
        RoadNetwork graph = new RoadNetwork("USA-road-NY");
        DijkstrasAlgorithm dijkstra = new DijkstrasAlgorithm(graph);
    
        TestingAlgorithms.doTesting(dijkstra, 1000, graph.getNumNodes(), "USA-road-NY", "Dijkstra");
    }
   */
    /*
    @Test
    public void testDoTesting6() throws Exception {
        System.out.println("doTesting6 ALT 1000");
        RoadNetwork graph = new RoadNetwork("USA-road-COL");
        LandmarkAlgorithm dijkstra = new LandmarkAlgorithm(graph);
    
        TestingAlgorithms.doTesting(dijkstra, 1000, graph.getNumNodes(), "USA-road-COL", "ALT");
    }
    */
    
    /*
    @Test
    public void testDoTesting3() throws Exception {
        System.out.println("doTesting3 CH 1000");
        RoadNetwork graph = new RoadNetwork("USA-road-CAL");
        ContractionHierarchies dijkstra = new ContractionHierarchies(graph);
    
        TestingAlgorithms.doTesting(dijkstra, 1000, graph.getNumNodes(), "USA-road-CAL", "CH");
    }
    */
    
    @Test
    public void testDoTesting4() throws Exception {
        System.out.println("doTesting4 ALT 1000");
        RoadNetwork graph = new RoadNetwork("USA-road-NY");
        LandmarkAlgorithm dijkstra = new LandmarkAlgorithm(graph);
    
        TestingAlgorithms.doTesting(dijkstra, 1000, graph.getNumNodes(), "USA-road-NY", "ALT");
    }
    
    /*
    @Test
    public void testEstimateDist() throws Exception {
        System.out.println("estimateDist ALT 1000");
        RoadNetwork graph = new RoadNetwork("USA-road-COL");
        LandmarkAlgorithm dijkstra = new LandmarkAlgorithm(graph);
    
        TestingAlgorithms.estimateDistALT(dijkstra, "USA-road-COL", "random", 1, 2, 4, 8, 16, 24, 32);
    }
    
    @Test
    public void testEstimateDist2() throws Exception {
        System.out.println("estimateDist2 ALT 1000");
        RoadNetwork graph = new RoadNetwork("USA-road-COL");
        LandmarkAlgorithm dijkstra = new LandmarkAlgorithm(graph);
    
        TestingAlgorithms.estimateDistALT(dijkstra, "USA-road-COL", "farthest", 1, 2, 4, 8, 16, 24, 32);
    }
    */
    /*
    @Test
    public void testDoTesting5() throws Exception {
        System.out.println("doTesting5 ALT 1000");
        RoadNetwork graph = new RoadNetwork("USA-road-COL");
        LandmarkAlgorithm dijkstra = new LandmarkAlgorithm(graph);
    
        TestingAlgorithms.doTesting(dijkstra, 1000, graph.getNumNodes(), "USA-road-COL", "ALT");
    }
    */
    
}
