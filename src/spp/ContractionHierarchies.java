package spp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import org.javatuples.Pair;

/**
 * Klasa koja realizira CH algoritam. 
 * Koristi se instanca klase DijkstrasAlgorithm za potrebe Dijkstrinog
 * pretrazivanj u dijelovima algoritma.
 * 
 * @author mandic
 */
public class ContractionHierarchies extends Algorithm{
    private RoadNetwork graph;
    protected DijkstrasAlgorithm dijkstra;
    //inverseRank[i] = v znaci da je rank(v) = i
    private ArrayList<Integer> inverseRank;
    //rank[v] = i znaci da je rank(v) = i
    protected ArrayList<Integer> rank;
    //Pamtimo za svaki vrh koliko je njemu susjednih vrhova izbrisano.
    private ArrayList<Integer> deletedNeighbours;
    //Strukture podataka koje sluze za brze racunanje procjene razlike bridova.
    private ArrayList<Integer> inNodesNotContracted;
    private ArrayList<Integer> outNodesNotContracted;
    //Pamti trenutno nekontrahirane vrhove i bridove.
    private int tmpNodes;
    private int tmpEdges;
    
    //Cuvamo prioritete vrhova, najmanji prioritet je na redu za kontrakciju.
    TreeSet<Pair<Integer, Integer>> priorities;
    //Dodatna struktura podataka koja pomaze kod realizacije azuriranja susjeda.
    HashMap<Integer, Integer> nodePriorMap;
    
    public ContractionHierarchies(RoadNetwork graph){
        this.graph = graph;
        dijkstra = new DijkstrasAlgorithm(graph);
        rank = new ArrayList<>(graph.getNumNodes());
        inverseRank = new ArrayList<>(graph.getNumNodes());
        deletedNeighbours = new ArrayList<>(graph.getNumNodes());
        
        inNodesNotContracted = new ArrayList<>(graph.getNumNodes());
        outNodesNotContracted = new ArrayList<>(graph.getNumNodes());
        
        tmpNodes = graph.getNumNodes();
        tmpEdges = graph.getNumEdges();
        
        priorities = new TreeSet<>();
        nodePriorMap = new HashMap<>();
        
        for(int i = 0; i < graph.getNumNodes(); ++i){
            rank.add(-1); //oznaka da jos nije postavljena vrijednost
            deletedNeighbours.add(0);
            inNodesNotContracted.add( graph.incomingArcs.get(i).size() );
            outNodesNotContracted.add( graph.outgoingArcs.get(i).size() );
        }
    }
    
    /**
     * Metoda koja vraca procjenu razlike bridova.
     * Kljucna stvar u ubrzanju predprocesiranja je konstantna vremenska slozenost ove metode.
     * Procjena je vrijedonst razlike bridova u najgorem slucaju -- kada je usitinu potrebno dodati precac
     * za svaki par ulaznog i izlaznog vrha.
     * 
     * @param v
     * @return
     * @throws Exception 
     */
    public int estimateEdgeDiff(int v) throws Exception{
        
        return inNodesNotContracted.get(v) * outNodesNotContracted.get(v) - inNodesNotContracted.get(v) - outNodesNotContracted.get(v);
    }
    
    /**
     * Provodi se kontrakcija vrha. 
     * 
     * @param v
     * @throws Exception 
     */
    public void contractNode(int v) throws Exception{
        //Pamtimo sve potrebne podatke o vrhovima u_i (inNodes) i vrhovima w_j (outNodes).
        ArrayList<Integer> inNodes = new ArrayList<>();
        ArrayList<Integer> inNodesCosts = new ArrayList<>();
        ArrayList<Integer> outNodes = new ArrayList<>();
        ArrayList<Integer> outNodesCosts = new ArrayList<>();
        
        for(Arc arc : graph.incomingArcs.get(v)){
            //Zanemarijemo sve bridove incidentne s vrhom v.
            //To postizemo promatrajuci za kojeje vrhove vec psotavljena rank vrijednost.
            if(rank.get(arc.anotherEndId) == -1){
                inNodes.add(arc.anotherEndId);
                inNodesCosts.add(arc.cost);
                
                deletedNeighbours.set(arc.anotherEndId, deletedNeighbours.get(arc.anotherEndId) + 1 );
                outNodesNotContracted.set(arc.anotherEndId, outNodesNotContracted.get(arc.anotherEndId) - 1);
            }
        }
        
        //Racunamo max(omega(v,w))) za sve izlazne vrhove w.
        int maxOutCost = Integer.MIN_VALUE;
        for(Arc arc : graph.outgoingArcs.get(v)){
            //Zanemarijemo sve bridove incidentne s vrhom v.
            if(rank.get(arc.anotherEndId) == -1){
                outNodes.add(arc.anotherEndId);
                outNodesCosts.add(arc.cost);
                
                if(arc.cost > maxOutCost)
                    maxOutCost = arc.cost;
                
                deletedNeighbours.set(arc.anotherEndId, deletedNeighbours.get(arc.anotherEndId) + 1 );
                inNodesNotContracted.set(arc.anotherEndId, inNodesNotContracted.get(arc.anotherEndId) - 1);
            }
        }
        
        //Uklanjamo sve bridove (u,v) i (v,w)
        tmpEdges -= (inNodes.size() + outNodes.size());
        tmpNodes--; //Uklonili smo vrh v.
        
        //Za svaki ulazni vrh u_i potrebno je napraviti modificiranu Dijsktra pretragu te
        //odrediti treba li dodati precac (u_i, w_j), za svaki izlazni vrh w_j.
        for(int i = 0; i < inNodes.size(); ++i){
            int u_i = inNodes.get(i);
            //Racunamo max_j(omega(u_i,v)+omega(v,w_j)) = omega(u_i,v) + max_j(omega(v,w_j)).
            int costupperbound = maxOutCost + inNodesCosts.get(i);
            dijkstra.costUpperBound = costupperbound;
            //Postavljena gornja ograda na udaljenost obradenog vrha.
            
            //Postavljamo i gornju ogradu na broj obradenih vrhova.
            dijkstra.maxNumSettledNodes = Integer.MAX_VALUE; //200;
            
            dijkstra.computeShortestPath(u_i, -1);
            //Dodajemo precac (u_i,w_j) ako i samo ako je dist[w_j] > omega(u_i,v) + omega(v,w_j).
            //Dodatno, kako bi izbjegli visestruke bridove potrebno je paziti treba li dodati novi precac
            //ili smanjiti tezinu briuda (u_i,w_j) ukoliko takav vec postoji.
            for(int j = 0; j < outNodes.size(); ++j){
                int w_j = outNodes.get(j);
                //bound = omega(u_i,v) + omega(v,w_j)
                int bound = inNodesCosts.get(i) + outNodesCosts.get(j);
                if(dijkstra.dist.get(w_j) > bound){
                    //Potrebno je dodati precac (u_i,w_j) tezine bound, ili smanjiti tezinu tog brida 
                    //ukoliko vec postoji.
                    if(graph.addArc(u_i, w_j, bound)){
                        tmpEdges++; //Dodali smo novi precac.
                        outNodesNotContracted.set(u_i, outNodesNotContracted.get(u_i) + 1);
                        inNodesNotContracted.set(w_j, inNodesNotContracted.get(w_j) + 1);
                    }
                }
            }
        }
        
        //Zakomentirati ukoliko se izvodi random rank poredak.
        //Azuriranje susjeda.
        HashSet<Integer> visitedNeighbours = new HashSet<>();
        for(Integer u : inNodes){
            visitedNeighbours.add(u);
            int tmp_prior = nodePriorMap.get(u);
            int new_prior = calculateNodePriority(u);
            if(tmp_prior != new_prior){
                nodePriorMap.put(u, new_prior);
                priorities.remove(new Pair<>(tmp_prior, u));
                priorities.add(new Pair<>(new_prior, u));
            }
        }
        for(Integer w : outNodes){
            if(!visitedNeighbours.contains(w)){
                visitedNeighbours.add(w);
                int tmp_prior = nodePriorMap.get(w);
                int new_prior = calculateNodePriority(w);
                if(tmp_prior != new_prior){
                    nodePriorMap.put(w, new_prior);
                    priorities.remove(new Pair<>(tmp_prior, w));
                    priorities.add(new Pair<>(new_prior, w));
                }
            }
        }
        //Kraj azuriranja susjeda.
        
    }
    
    /**
     * Obavlja predprocesiranje: odreduje poredak (rank vrijednosti) vrhova,
     * te tim redom provodi kontrakciju vrhova. Tim postupkom dodaju se 
     * potrebni bridovi precaci u grafu. Na kraju, postavlja arcFlag na false
     * svim bridovima koji nisu uzlazni (u outgoing i incoming listama Dijkstrinog 
     * algoritma), cime je sve spremno za fazu upita.
     * Argumenti metode su nepotrebni, no ostavljamo ih kako bi klsa mogla naslijediti klasu Algorithm
     * zbog lakseg testiranja i usporedivanja.
     */
    @Override
    public void preprocess(int ai, String ss) throws Exception{
        //Postavljamo oznaku kako treba uzeti u obzir zastavice arcFlags koje olaksavaju
        //prividno izbacivanje bridova iz grafa.
        dijkstra.considerArcFlags = true;
        
        dijkstra.setContractionHierarchiesAlgorithm(this);
        
        //Odkomentirati ukoliko se koristi random rank poredak.
        /*
        calculateRandomInverseRankPermutaion();
        */
        
        //Zakomentirati ukoliko se izvodi random rank poredak.
        //Na pocetku dodemo sve vrhove u prioritetni red s pripadnom prioritetnom vrijednosti.
        for(int i = 0; i < graph.getNumNodes(); ++i){
            int tmp_prior = calculateNodePriority(i);
            priorities.add(new Pair<>(tmp_prior, i));
            nodePriorMap.put(i, tmp_prior);
        }
        
        
        //Racunamo za koji ce vrh v biti rank(v) = i, tj. inverseRank(i) = v.
        for(int i = 0; i < graph.getNumNodes(); ++i){
            /*
            if(i % 2_000 == 0 || (graph.getNumNodes() - i) < 50){
                System.out.println("Provedena kontrakcija nad " + i + " vrhova.");
                System.out.println("\tUkupno bridova " + getNumberOfEdges() + " bridova.");
                System.out.println("\tTrenutni prosjecni stupanj vrhova: " + String.format("%.2f", ((double)(2 * tmpEdges))/tmpNodes) + ", tmpEdges = " + tmpEdges + ", tmpNodes = " + tmpNodes);
            }
            */
            
            /*
            //Odkomentirati ukoliko se koristi random rank poredak.
            int v = inverseRank.get(i);
            rank.set(v, i);
            contractNode(v);
            */
            
            //Zakomentirati ukoliko se izvodi random rank poredak.
            //Sljedeci na redu je uvijek vrh s najmanjim prioritetom. 
            int v = priorities.pollFirst().getValue1();
            
            
            //Zakomentirati ukoliko se izvodi random rank poredak.
            //Obavljamo lijeno azuriranje.
            int prior = calculateNodePriority(v);
            nodePriorMap.put(v, prior);
            if(i != graph.getNumNodes() - 1){
                while(prior > priorities.first().getValue0()){
                    priorities.add(new Pair<>(prior, v));
                    v = priorities.pollFirst().getValue1();
                    prior = calculateNodePriority(v);
                    nodePriorMap.put(v, prior);
                }   
            }
            
            //Zakomentirati ukoliko se izvodi random rank poredak.
            //Obavlja se kontrakcija vrha v. Vrijedi rank(v) = i.
            rank.set(v, i);
            inverseRank.add(v);
            nodePriorMap.remove(v, prior);//Moguca greska ukoliko postoji self-loop.
            contractNode(v);
            
            //Azuriranje susjeda provodi se nakon izbacivanja vrha u contractNode metodi.
        }
        
        //PROVJERA
        ArrayList<Integer> check = new ArrayList<>(graph.getNumNodes());
        for(int i = 0; i < graph.getNumNodes(); ++i)
            check.add(1);
        //Provjeravmo da je svaka vrijednost odabrana tocno jednom.
        for(int r : rank){
            check.set(r, check.get(r) - 1);
            if(0 != (int)check.get(r))
                throw new Exception("NETOCNO ODREDIVANJE RANK FUNKCIJE!");
        }
        //END PROVJERA
        
        //Kreiranje G*_uparrow i G*_downarrow grafova.
        //U listi outgoing ostavimo arcFlag = true samo kod onih bridova (u,v) gdje je rank(u)<rank(v).
        //Slicno, kod liste incoming ostavimo arcFlag=true samo kod bridova (u,v) gje je rank(u)<rank(v).
        for(int u = 0; u < graph.getNumNodes(); ++u){
            ArrayList<Arc> outArcs = graph.outgoingArcs.get(u);
            ArrayList<Arc> inArcs = graph.incomingArcs.get(u);
            for(Arc arc : outArcs){
                int v = arc.anotherEndId;
                if( rank.get(u) < rank.get(v) )
                    arc.arcFlag = true;
                else
                    arc.arcFlag = false;
            }
            for(Arc arc : inArcs){
                int v = arc.anotherEndId;
                if( rank.get(u) < rank.get(v) )
                    arc.arcFlag = true;
                else
                    arc.arcFlag = false;
            }
        }
    }
    
    /**
     * Metoda koja racuna na slucajan nacin rank vrijednosti vrhova.
     * Time dobijemo slucajan poredak kontrakcije bridova.
     * 
     * @throws Exception 
     */
    public void calculateRandomInverseRankPermutaion() throws Exception{
        for(int i = 0; i < graph.getNumNodes(); ++i)
            inverseRank.add(i);
        Random rnd = new Random();
        for(int i = graph.getNumNodes() - 1; i > 0; --i){
            int changeIndex = rnd.nextInt(i);
            int tmpInt = inverseRank.get(changeIndex);
            inverseRank.set(changeIndex, inverseRank.get(i));
            inverseRank.set(i, tmpInt);
        }
        
        //PROVJERA
        ArrayList<Integer> check = new ArrayList<>(graph.getNumNodes());
        for(int i = 0; i < graph.getNumNodes(); ++i)
            check.add(1);
        //Provjeravmo da je svaka vrijednost odabrana tocno jednom.
        for(int r : inverseRank){
            check.set(r, check.get(r) - 1);
            if(0 != (int)check.get(r))
                throw new Exception("NETOCNO ODREDIVANJE RANK FUNKCIJE!");
        }
    }
    
    @Override
    public int computeShortestPath(int sourceNodeId, int targetnodeId){
        return dijkstra.computeShortestPathCH(sourceNodeId, targetnodeId);
    }
    
    /**
     * Racuna prioritet vrha v. 
     * Moguce razne opcije. Kao prioritet
     * uzimamo linearnu kombinaciju razlike vrhova i 
     * broja obrisanih/kontrahiranih susjeda.
     * 
     * @param v
     * @return Prioritet vrha v.
     */
    private int calculateNodePriority(int v) throws Exception{
        int edgeDiff = estimateEdgeDiff(v);
        int delNeighbours = deletedNeighbours.get(v);
        //Moguce razne varijante. Vratiti tezinsku sumu.
        return   2 * edgeDiff + 1 * delNeighbours;
    }
    
    public String getRankString(){
        StringBuilder sb = new StringBuilder("(");
        sb.append(inverseRank.get(0));
        for(int i = 1; i < inverseRank.size(); ++i)
            sb.append("," + inverseRank.get(i));
        sb.append(")");
        return sb.toString();
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
}
