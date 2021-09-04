package spp;

import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Klasa koja sadrzi jedinicne testove za reprezentaciju grafa - RoadNetwork.
 * 
 * @author mandic
 */
public class RoadNetworkTest {
    
    public RoadNetworkTest() {
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
     * Test of toString method, of class RoadNetwork.
     */
    @Test
    public void testToString() {
        System.out.println("toString test");
        RoadNetwork instance = new RoadNetwork();
        String expResult = "[]";
        String result = instance.toString();
        assertEquals(expResult, result);
        
        instance.addNode(0, 0, 0);
        assertEquals("[0]", instance.toString());
        
        instance.addNode(1,0,0);
        assertEquals("[0,1]", instance.toString());
        
        instance.addArc(0, 1, 5);
    
        assertEquals("[0,1,(0,1,5)]", instance.toString());
    }

    /**
     * Test of addNode method, of class RoadNetwork.
     */
    @Test
    public void testAddNode() {
        System.out.println("addNode test");
        int nodeId = 0;
        int latitude = 0;
        int longitude = 0;
        RoadNetwork instance = new RoadNetwork();
        instance.addNode(nodeId, latitude, longitude);
        // TODO review the generated test code and remove the default call to fail.
        assertEquals(1, instance.getNumNodes());
        
        instance.addNode(2, 0, 0);
        assertEquals(2, instance.getNumNodes());
        assertEquals(0, instance.getNumEdges());
    }

    /**
     * Test of addArc method, of class RoadNetwork.
     */
    @Test
    public void testAddArc(){
        System.out.println("addArc test");
        int u = 0;
        int v = 1;
        int cost = 0;
        RoadNetwork instance = new RoadNetwork();
        instance.addNode(0, 0, 0);
        instance.addNode(1, 0, 0);
        instance.addArc(u, v, cost);
        
        assertEquals(1, instance.getNumEdges());
        
        instance.addArc(v, u, 12);
        assertEquals(2, instance.getNumEdges());
    }
    
    /**
     * Test of readGraphFromFile method, of class RoadNetwork.
     */
    @Test
    public void testReadGraphFromFile() throws IOException{
        System.out.println("readGraphFromFile test");
        RoadNetwork instance = new RoadNetwork();
        instance.readGraphFromFile("simpleGraphTest");
        
        assertEquals("[0,1,2,(0,1,400),(2,1,100)]", instance.toString());
        assertEquals(3, instance.getNumNodes());
        assertEquals(2, instance.getNumEdges());
    }
    
}
