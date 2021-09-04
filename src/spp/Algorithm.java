package spp;

/**
 * Ova klasa služi za jednostavnije izvršavanje algoritama. Objedinjuje pojedine
 * vazne metode. Ukoliko neki od algoritma ne zahtijeva neku od njih, npr DIjkstrin algoritam
 * ne zahtijeva predprocesiranje, implementacija metoed ne radi nista. 
 * 
 * @author mandic
 */
public abstract class Algorithm {
    abstract int computeShortestPath(int sourceNodeId, int targetNodeId);
    abstract void preprocess(int numLandmarks, String option) throws Exception;
    abstract int getNumberOfSettledNodes();
    abstract int getNumberOfNodes();
    abstract int getNumberOfEdges();
}
