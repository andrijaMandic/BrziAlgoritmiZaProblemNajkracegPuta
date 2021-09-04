package spp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;
import org.javatuples.Pair;

/**
 * Klasa koja reprezentira vrh grafa. Dodane su vrijednisti geografske duljine i 
 * sirine, sto se moze iskoristiti kod procjene udaljenosti. Medutim, u implementiranim
 * algoritmima to ipak nece biti potrebno, vec ce biti vazan jedinstveni indeks vrha.
 * @author mandic
 */
class Node {
    int nodeId;
    int latitude;
    int longitude;

    Node(int nodeId, int la, int lo){
        this.nodeId = nodeId;
        this.latitude = la;
        this.longitude = lo;
    }
    
    Node(Node vertex){
        this.nodeId = vertex.nodeId;
        this.latitude = vertex.latitude;
        this.longitude = vertex.longitude;
    }
  
}

/**
 * Klasa koja reprezentira usmjereni brid grafa. Kako se bridovi cuvaju u listi susjedstva,
 * jedan vrh je vrh indeksa iz liste, dok je drugi spremljen u klasi Arc.
 * Logicka varijabla arcFlag koristit ce se u CH algoritmu kao realizacija brisanja pojedinih bridova.
 * Ukoliko treba promatrati tu varijablu, brid se zanmaruje ako ima vrijednost false.
 * 
 * @author mandic
 */
class Arc {
    Arc(int headNodeId, int cost){
        this.anotherEndId = headNodeId;
        this.cost = cost;
        this.arcFlag = true;
    }
    Arc(Arc edge){
        this.anotherEndId = edge.anotherEndId;
        this.cost = edge.cost;
        this.arcFlag = edge.arcFlag;
    }
    
    int anotherEndId;
    int cost;
    //Sluzi za "preskakanje" bridova oznacenih s arcFlag = false.
    boolean arcFlag;
}

/**
 * Klasa koja implementira stvari potrebne za prikaz grafa na racunalu. Svi algoritmi 
 * koristit ce objekt iz klse RoadNetwork za realizaciju grafa.
 * 
 * @author mandic
 */
public class RoadNetwork {
    private int numNodes;
    protected int numEdges;
    protected ArrayList<ArrayList<Arc>> incomingArcs;
    protected ArrayList<ArrayList<Arc>> outgoingArcs;
    private ArrayList<Node> nodes;
    
    public RoadNetwork(){
        numNodes = numEdges = 0;
        incomingArcs = new ArrayList<>();
        outgoingArcs = new ArrayList<>();
        nodes = new ArrayList<>();
    }
    
    public RoadNetwork(String filename){
        numNodes = numEdges = 0;
        try{
        readGraphFromFile(filename);
        }
        catch(IOException e){
            System.err.println(e);
            System.out.println("Greska kod ucitavanja grafa iz filea " + filename);
        }
    }
    
    public RoadNetwork(RoadNetwork graph){
        numNodes = graph.getNumNodes();
        numEdges = graph.getNumEdges();
        incomingArcs = new ArrayList<>();
        outgoingArcs = new ArrayList<>();
        nodes = new ArrayList<>();
        
        for(int i = 0; i < graph.nodes.size(); ++i){
            nodes.add(new Node(graph.nodes.get(i)));
            incomingArcs.add(new ArrayList<>());
            outgoingArcs.add(new ArrayList<>());
            for(Arc a : graph.incomingArcs.get(i))
                incomingArcs.get(i).add(new Arc(a));
            for(Arc a : graph.outgoingArcs.get(i))
                outgoingArcs.get(i).add(new Arc(a));
        }
    }
   
    @Override
    public String toString(){
        StringBuilder res = new StringBuilder();
        res.append("[");
        for(int i = 0; i < nodes.size() - 1; ++i)
            res.append("" + nodes.get(i).nodeId + ",");
        if(nodes.size() > 0)
            res.append("" + nodes.get(nodes.size() - 1).nodeId);
        
        //Set of incomingArcs is equal to set of outgoingArcs.
        for(int i = 0; i < outgoingArcs.size(); ++i){
            ArrayList<Arc> outNodes = outgoingArcs.get(i);
            for(int j = 0; j < outNodes.size(); ++j)
                res.append(",(" + nodes.get(i).nodeId + "," + outNodes.get(j).anotherEndId + "," + outNodes.get(j).cost + ")");
        }
        res.append("]");
        return res.toString();
    }
    
    public void addNode(int nodeId, int latitude, int longitude){
        nodes.add( new Node(nodeId, latitude, longitude) );
        numNodes++;
        outgoingArcs.add(new ArrayList<>());
        incomingArcs.add(new ArrayList<>());

    }
    
    public boolean eraseArc(int u, int v){
        boolean exists = false;
        for(int i = 0; i < outgoingArcs.get(u).size(); ++i){
            if(outgoingArcs.get(u).get(i).anotherEndId == v){
                outgoingArcs.get(u).remove(i);
                exists = true;
                numEdges--;
                break;
            }
        }
        if(exists){
            for(int i = 0; i < incomingArcs.get(v).size(); ++i){
                if(incomingArcs.get(v).get(i).anotherEndId == u){
                    incomingArcs.get(v).remove(i);
                    break;
                }
            }
        }
        return exists;
    }
    
    /**
     * Dodajemo usmjereni brid grafa. Vodimo racuna da ne dodamo vec postojeci brid, tj.
     * graf ne smije sadrzavati visestruke bridove. Ukoliko dodajemo vec postojeci brid ali manje vrijednosti,
     * tu vrijednost zapisemo kao novu tezinu brida.
     * 
     * @param u
     * @param v
     * @param cost
     * @return true ukoliko je dodan novi brid, false u suprotnom 
     */
    public boolean addArc(int u, int v, int cost){
        //Pazimo kako ne bi dodali visestruke bridove. 
        boolean exists = false;
        //less = (cost < arc.cost), tj ako je less==true onda treba promijeniti tezinu brida.
        boolean less = false;
        for(Arc arc : outgoingArcs.get(u)){
            if(arc.anotherEndId == v){
                //Postavljamo jedinstveni brid izmedu dva vrha uzimajuci najkraci od ponudenih.
                if(arc.cost > cost){
                    arc.cost = cost;
                    less = true;
                }
                exists = true;
                break;
            }
        }
        if(exists && less){
            //Potrebno promijeniti tezinu dog brida i u incoming listi.
            for(Arc arc : incomingArcs.get(v)){
                if(arc.anotherEndId == u){
                    arc.cost = cost;
                    break;
                }
            }
        }
        if(!exists){
            //Ne postoji takav brid, dodajemo novi.
            outgoingArcs.get(u).add( new Arc(v, cost) );
            incomingArcs.get(v).add( new Arc(u, cost) );
            numEdges++;
            return true;
        }
        return false;
    }
    
    public int getNumNodes(){
        return numNodes;
    }
    public int getNumEdges(){
        return numEdges;
    }
    
    /**
     *  Funkcija koja ucitava graf iz txt filea. Svi fileovi moraju se nalaziti u 
     *  data folderu unutar root direktorija projekta.
     * @param fileName 
     */
    public void readGraphFromFile(String fileName) throws IOException{
        BufferedReader brCo = new BufferedReader(new FileReader( "data//" + fileName + "-co.txt" ));
       //Ucitavanje svih vrhova.
        String line = brCo.readLine();
        int cnt = 0;
        while(line != null){
            StringTokenizer st = new StringTokenizer(line);
            String header = st.nextToken();
            if(header.equals("v")){
                //vrhovi su numerirani u podacima od 1, stoga oduzimamo 1 kako bi
                //bili oznaceni s 0,1...
                int node = Integer.parseInt(st.nextToken()) - 1;
                int longitude = Integer.parseInt(st.nextToken());
                int latitude = Integer.parseInt(st.nextToken());
                this.addNode(node, latitude, longitude);
                if(node != cnt)
                    throw new IOException("GRESKA U PODACIMA KOD UCITAVNJA VRHOVA");
                cnt++;
            }
            else if(header.equals("p")){
                for(int i = 0; i < 3; ++i) st.nextToken();
                int numberOfNodes = Integer.parseInt( st.nextToken() );
                
                nodes = new ArrayList<>(numberOfNodes);
                incomingArcs = new ArrayList<>(numberOfNodes);
                outgoingArcs = new ArrayList<>(numberOfNodes);
            }
            line = brCo.readLine();
        }
        brCo.close();
        
        BufferedReader brGr = new BufferedReader(new FileReader("data//" + fileName + "-gr.txt"));
        //Ucitavanje svih bridova.
        line = brGr.readLine();
        while(line != null){
            StringTokenizer st = new StringTokenizer(line);
            String header = st.nextToken();
            if(header.equals("a")){
                //vrhovi su numerirani u podacima od 1, stoga oduzimamo 1 kako bi
                //bili oznaceni s 0,1...
                Integer u = Integer.parseInt(st.nextToken()) - 1;
                Integer v = Integer.parseInt(st.nextToken()) - 1;
                Integer cost = Integer.parseInt(st.nextToken());
                this.addArc(u, v, cost);
            }
            line = brGr.readLine();
        }
        brGr.close();
    }
}
