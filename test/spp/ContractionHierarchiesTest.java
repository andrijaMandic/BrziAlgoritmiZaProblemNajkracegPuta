package spp;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Klasa za jedinicne testove CH algoritma.
 * 
 * @author mandic
 */
public class ContractionHierarchiesTest {
    
    public ContractionHierarchiesTest() {
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

    //Testovi koji se modificiraju po potrebi na pocetku razvijanja CH algoritma.
    //Kako se mijenjaju parametri za odredivanje vrijednosti vrhova pri kontrakciji,
    //ostavljamo testove pod komentarima posto ne moraju uvijek davati trazena rjesenja
    //(ovise o odabiru parametara).
    /*
    @Test
    public void testContractNode(){
        System.out.println("contractNode test");
        
        RoadNetwork graph = new RoadNetwork("contractGraphTest");
        ContractionHierarchies ch = new ContractionHierarchies(graph);
        
        ch.dijkstra.considerArcFlags = true;
        ch.dijkstra.setContractionHierarchiesAlgorithm(ch);
        ch.contractNode(2, false);
        assertEquals(7, graph.numEdges);
        //assertEquals("[0,1,2,3,4,(0,2,1),(0,3,3),(1,2,1),(1,3,3),(1,4,4),(2,3,2),(2,4,3),(3,4,1),(4,3,1)]", graph.toString());
        assertEquals("[0,1,2,3,4,(0,2,1),(0,3,3),(1,2,1),(2,3,2),(2,4,3),(3,4,1),(4,3,1)]", graph.toString());
    }
    
    @Test
    public void testContractNodeED(){
        System.out.println("contractNode test");
        
        RoadNetwork graph = new RoadNetwork("contractGraphTest");
        ContractionHierarchies ch = new ContractionHierarchies(graph);
        
        ch.dijkstra.considerArcFlags = true;
        ch.dijkstra.setContractionHierarchiesAlgorithm(ch);
        assertEquals(-4, ch.contractNode(2, true));
        assertEquals(-4, ch.contractNode(2, true));
    }
    */
    
    @Test
    public void testPreprocess() throws Exception{
        System.out.println("preprocess test");
        RoadNetwork graph = new RoadNetwork("contractGraphTest");
        ContractionHierarchies ch = new ContractionHierarchies(graph);
        
        ch.preprocess(5, "visak");
        ArrayList<Integer> rank = ch.rank;
        ArrayList<Integer> check = new ArrayList<>(graph.getNumNodes());
        for(int i = 0; i < graph.getNumNodes(); ++i)
            check.add(1);
        //Provjeravmo da je svaka vrijednost odabrana tocno jednom.
        for(int r : rank){
            check.set(r, check.get(r) - 1);
            assertEquals(0, (int)check.get(r));
        }
    }
    
    @Test
    public void testComputeShortestpath() throws Exception{
        System.out.println("computeShortestPath  test");
        
        RoadNetwork graph = new RoadNetwork("contractGraphTest");
        ContractionHierarchies instance = new ContractionHierarchies(graph);
        instance.preprocess(5, "visak");
        //System.out.println(instance.getRankString());
        //System.out.println(graph.toString());
        
        assertEquals(0, instance.computeShortestPath(0, 0));
        assertEquals(3, instance.computeShortestPath(0, 3));
        assertEquals(4, instance.computeShortestPath(0, 4));
        assertEquals(4, instance.computeShortestPath(1, 4));
        assertEquals(Integer.MAX_VALUE, instance.computeShortestPath(3, 0));
    }
    
    @Test
    public void testComputeShortestpath2() throws Exception{
        System.out.println("computeShortestPath2  test");
        
        RoadNetwork graph = new RoadNetwork("chGraphTest");
        ContractionHierarchies instance = new ContractionHierarchies(graph);
        instance.preprocess(5, "visak");
        //System.out.println(instance.getRankString());
        //System.out.println(graph.toString());
        //System.out.println("Broj vrhova " + graph.getNumEdges());
        
        assertEquals(0, instance.computeShortestPath(2, 2));
        assertEquals(10, instance.computeShortestPath(0, 7));
        assertEquals(10, instance.computeShortestPath(7, 0));
        assertEquals(7, instance.computeShortestPath(1, 6));
        assertEquals(6, instance.computeShortestPath(7, 6));
        assertEquals(8, instance.computeShortestPath(3, 0));
        assertEquals(2, instance.computeShortestPath(2, 5));
        assertEquals(8, instance.computeShortestPath(7, 1));
    }
    
     @Test
    public void testComputeShortestpath3() throws Exception{
        System.out.println("computeShortestPath3  test");
        
        RoadNetwork graph = new RoadNetwork();
        graph.addNode(0, 0, 0);
        graph.addNode(1, 0, 0);
        graph.addNode(2, 0, 0);
        graph.addNode(3, 0, 0);
        graph.addNode(4, 0, 0);
        graph.addNode(5, 0, 0);
        graph.addNode(6, 0, 0);
        graph.addArc(0, 1, 1);
        graph.addArc(1, 2, 1);
        graph.addArc(2, 3, 1);
        graph.addArc(3, 4, 1);
        graph.addArc(4, 5, 1);
        graph.addArc(5, 6, 1);
        ContractionHierarchies instance = new ContractionHierarchies(graph);
        instance.preprocess(5, "visak");
        //System.out.println(instance.getRankString());
        //System.out.println(graph.toString());
        //System.out.println("Broj bridova " + graph.getNumEdges());
        
        assertEquals(0, instance.computeShortestPath(2, 2));
        assertEquals(2, instance.computeShortestPath(1, 3));
        assertEquals(6, instance.computeShortestPath(0, 6));
        assertEquals(Integer.MAX_VALUE, instance.computeShortestPath(5, 2));
    }
    
    
    @Test
    public void testComputeShortestpathComplex() throws Exception{
        System.out.println("computeShortestPath complex test");
        
        RoadNetwork graph = new RoadNetwork("kusalicGraphTest");
        ContractionHierarchies instance = new ContractionHierarchies(graph);
        instance.preprocess(5, "visak");
        //System.out.println(instance.getRankString());
        
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
