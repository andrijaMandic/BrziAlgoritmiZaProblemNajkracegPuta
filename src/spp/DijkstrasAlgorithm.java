package spp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import org.javatuples.Pair;

/**
 * Klasa DijkstrasAlgorithm implementira sve potrebne stvari za realizaciju Dijkstrinog algoritma,
 * te potrebne metode za provedbu faze upita CH i ALT algoritma.
 * 
 * @author mandic
 */
public class DijkstrasAlgorithm extends Algorithm {
    private RoadNetwork graph;
    private ArrayList<Integer> parents;
    private HashSet<Integer> visitedNodes;
    private HashSet<Integer> settledNodes;
    protected ArrayList<Integer> dist;
    private ArrayList<Integer> heuristic;
    private boolean reverseSearch;
    //Koristimo sortirani set kako bismo ubrzali pronalazak najblizek neobradenog vrha.
    private TreeSet<Pair<Integer, Integer>> nearestNodes;
    //Cuvamo referencu na LandmarkAlgorithm posto treba koristiti funkciju racunanja heuristike.
    //U prvotnoj implementaciji LandmarkAlgorithm je racunao vrijednosti heuristike za sve vrhove te
    //polje heuristic postavljao direktno. Medutim, takva implementacija daje sporije rezultate.
    protected LandmarkAlgorithm altAlgorithm;
    protected ContractionHierarchies chAlgorithm;
    protected boolean heuristicSearch;
    
    //Dodatne varijable potrebne kod pretrazivanja unazad koje se koristi u dvosmjernom Dijsktrinom pretrazivanju.
    private HashSet<Integer> visitedNodesReverseSearch;
    private HashSet<Integer> settledNodesReverseSearch;
    private ArrayList<Integer> distReverseSearch;
    private TreeSet<Pair<Integer, Integer>> nearestNodesReverseSearch;
    private ArrayList<Integer> parentsReverseSearch;
    
    //Varijable potrebne kod "malih" pretrazivanja kod kontrakcije vrhova ContractionHierarchies algoritma.
    protected boolean considerArcFlags; //Provodi relaksaciju samo onih vrhova kod kojih je zastavica postavljena na true.
    protected int costUpperBound; //Ukoliko se obradi vrh preko zadane granice, zaustavlja se pretrazivnjae. Default MAX_VALUE.
    protected int maxNumSettledNodes; //Default MAX_VALUE. Koristi se kod ogranicavanja pretrage u predprocesiranju CH algoritma.
    
    public DijkstrasAlgorithm(RoadNetwork graph){
        this.graph = graph;
        parents = new ArrayList<>(graph.getNumNodes());
        dist = new ArrayList<>(graph.getNumNodes());
        heuristic = new ArrayList<>(graph.getNumNodes());
        visitedNodes = new HashSet<>(graph.getNumNodes());
        settledNodes = new HashSet<>(graph.getNumNodes());
        nearestNodes = new TreeSet<>();
        reverseSearch = false;
        
        //Inicijalizacija varijabli potrebnih za dvosmjerno Dijkstrino pretrazivanje.
        visitedNodesReverseSearch = new HashSet<>(graph.getNumNodes());
        settledNodesReverseSearch = new HashSet<>(graph.getNumNodes());
        distReverseSearch = new ArrayList<>(graph.getNumNodes());
        nearestNodesReverseSearch = new TreeSet<>();
        parentsReverseSearch = new ArrayList<>(graph.getNumNodes());
        
        for(int i = 0; i < graph.getNumNodes(); ++i){
            parents.add(-1);
            dist.add(Integer.MAX_VALUE);
            heuristic.add(-1); //-1 oznaka kako heuristicka procjena nije pronadena
            parentsReverseSearch.add(-1);
            distReverseSearch.add(Integer.MAX_VALUE);
        }
        
        //Postavit cemo vrijednost posebnom metodom. U obicnom Dijkstrinom pretrazivnju nije potrebno 
        //imati postavljeno ovu varijblu.
        altAlgorithm = null;
        chAlgorithm = null;
        heuristicSearch = false;
        
        //CH algoritam - potrebne varijable.
        considerArcFlags = false;
        costUpperBound = Integer.MAX_VALUE;
        maxNumSettledNodes = Integer.MAX_VALUE;
    }
    
    /**
     * Funkcija koja racuna vrijednost najkraceg puta do odredisnog vrha.
     * Ukoliko je target = -1, racuna do svih preostalih vrhova. Ukoliko je postavljena
     * zastavica za pretragu unazad, odraduje takvo pretrazivanje. Ovisno i o 
     * zastavici koja odreduje heuristicko pretrazivanje, ukoliko je postavljena, 
     * racuna A* algoritam.
     * 
     * @param sourceNodeId
     * @param targetNodeId
     * @return tezina najkraceg puta 
     */
    @Override
    int computeShortestPath(int sourceNodeId, int targetNodeId){
        
        ArrayList<Integer> sources = new ArrayList<>();
        sources.add(sourceNodeId);
        return computeShortestPathMultipleSources(sources, targetNodeId);
    } 
    
    /**
     * Odraduje iste zadatke kao obicna Dijsktrina pretraga, samo uz dopustenje
     * vise vrhova izvora.
     * 
     * @param sources
     * @param targetNodeId
     * @return Najmanja udaljenost do nekog od vrhova izvora (source nodes).
     */
    protected int computeShortestPathMultipleSources(ArrayList<Integer> sources, int targetNodeId){
        //Na pocetku postavi dist, settledNodes i visitedNodes na pocetne vrijednosti.
        Iterator<Integer> it = visitedNodes.iterator();
        while(it.hasNext()){
            int nodeId = it.next();
            dist.set(nodeId, Integer.MAX_VALUE);
            parents.set(nodeId, -1);
            if(heuristicSearch)
                heuristic.set(nodeId, -1);
        }
        visitedNodes.clear();
        settledNodes.clear();
        nearestNodes.clear();
        
        int N = this.graph.getNumNodes();
        for(Integer sourceNodeId : sources){
            visitedNodes.add(sourceNodeId);
            dist.set(sourceNodeId, 0);
        
            if(!heuristicSearch) //Dijsktra pretrazivanje.
                nearestNodes.add(new Pair(0, sourceNodeId));
            else{
                //Ukoliko jos nije izracunata vrijednost, racunamo je funkcijom iz Landmarkalgorithm objekta.
                if(heuristic.get(sourceNodeId) == -1)
                    heuristic.set(sourceNodeId, altAlgorithm.calculateHeuristicFunction(sourceNodeId, targetNodeId));
                //AStar pretrazivanje, prioritetu vrha v dodamo h(v).
                nearestNodes.add(new Pair(0 + heuristic.get(sourceNodeId), sourceNodeId));
            } 
        }
        //Nije bitna pocetna vrijednost posto se odmah na pocetku pronalazi ponovno najmanji vrh.
        int cvorId = -1;
        
        //Dodajemo dodatni uvijet na broj iteracija. Kako se u svakoj iteraciji obradi tocno jedan vrh, provjeravamo
        //je li broj obradenih vrhova presao limit maxNumSettledNodes. Ovaj uvjet koristi se kod CH algoritma
        //u fazi predprocesiranja kako bi ogranicio prostor pretrage. Pocetna, defaultna vrijednost je MAX_VALUE
        //stoga ne utjece na ispravnost klasicne Dijkstrine pretrage.
        for(int i = 0; i < N && i < maxNumSettledNodes; ++i){
            //Pronadi sljedeci najblizi posjecen a neobraden vrh.
            //Ukoliko nismo pronasli rjesenje i zakljucimo da ne postoji put do cilja - vracamo MAX_INT.
            if(nearestNodes.isEmpty())
                return Integer.MAX_VALUE;
            cvorId = nearestNodes.pollFirst().getValue1();
            //Oznaci cvorId oznakom obraden.
            settledNodes.add(cvorId);
            
            //Dodatna provjera potrebna kod CH algoritma. Ukoliko je obraden vrh s cijenom vecom od
            //maksimalne dozvoljene, prekini pretragu. Defaultna vrijednost je MAX_VALUE, stoga ovaj
            //uvjet ne ujece na normalno izvrsavanje Dijkstrinog pretrazivanja.
            if(dist.get(cvorId) > costUpperBound)
                break;
            
            if(targetNodeId == cvorId)
                return dist.get(targetNodeId);
            
            //Pokusaj popraviti rjesenje putom do prostalih vrhova preko vrha cvorId.
            ArrayList<Arc> outNodes;
            if(!reverseSearch)
                outNodes = this.graph.outgoingArcs.get(cvorId);
            else
                outNodes = this.graph.incomingArcs.get(cvorId);
            
            for(Arc arc : outNodes){
                //Dodatna provjera potrebna kod CH algoritma. Ukoliko se gledaju zastavice i ukoliko je takva false
                //tada ne provodi relaksaciju tog brida.
                //Ukoliko je postavljena rank vrijednost drugig vrha promatranog brida, znamo da je takav vec obraden te ga
                //zanemarujemo.
                if(considerArcFlags && (chAlgorithm.rank.get(arc.anotherEndId) != -1)){
                    //System.out.println("DIJKSTRA NEMA DOBAR IN OUT LIST VRHOVA!!!!!!!!!!!!!!!!!");
                    continue;
                }
                
                int headArcNode = arc.anotherEndId;
                if(!settledNodes.contains(headArcNode) && (dist.get(headArcNode) > dist.get(cvorId) + arc.cost)){
                    if(!heuristicSearch)//Dijkstra pretrazivanje.
                        nearestNodes.remove(new Pair(dist.get(headArcNode), headArcNode));
                    else{//AStar pretrazivanje.
                        //Ukoliko do sad nismo izracunali heuristicku funkciju, pozivamo racunanje iz LandmarkAlgorithm objekta.
                        if(heuristic.get(headArcNode) == -1)
                            heuristic.set(headArcNode, altAlgorithm.calculateHeuristicFunction(headArcNode, targetNodeId));
                        
                        nearestNodes.remove(new Pair(dist.get(headArcNode) + heuristic.get(headArcNode), headArcNode));
                    }
                    dist.set(headArcNode, dist.get(cvorId) + arc.cost);
                    if(!heuristicSearch)//Dijkstra pretrazivanje
                        nearestNodes.add(new Pair(dist.get(headArcNode), headArcNode));
                    else{//AStar pretrazivanje
                        //Vrijednost heuristike postavljena u ovoj iteraciji.
                        nearestNodes.add(new Pair(dist.get(headArcNode) + heuristic.get(headArcNode), headArcNode));
                    }
                    if(!visitedNodes.contains(headArcNode))
                        visitedNodes.add(headArcNode);
                    parents.set(headArcNode, cvorId);
                }
            }

        }
        //Vraca MAX_INT ukoliko nije pronaden najkraci put ili je targetNode == -1.
        return Integer.MAX_VALUE;
    }
    
    /**
     * Dvosmjerno Dijkstrino pretrazivanje.
     * Koriste se dva prioritetna reda - skupa, gdje se uvijek za iduci korak uzima
     * manji iz oba. Kod pretrazivanja unazad koriste se incomingArcs bridovi iz RoadNetwork objekta.
     * U ovoj metodi racunamo Dijkstrino pretrazivanje, tj. bez dodatnih heuristika kod procjene
     * ciljne udaljenosti.
     * 
     * @param sourceNodeId
     * @param targetNodeId
     * @return Najkraci put od sourceNodeId do targetNodeId vrha.
     */
    public int bidirectionalSearch(int sourceNodeId, int targetNodeId){
        //Osiguravamo da su zadani vrhovi razliciti.
        if(sourceNodeId == targetNodeId)
            return 0;
        
        //Na pocetku postavi potrebne varijable na pocetne vrijednosti.
        //Postavljamo samo promijenjene vrijednosti, kako ne bi bespotrebno azurirali podatke za svaki vrh.
        //Uzimamo u obzir i pretragu unazad.
        Iterator<Integer> it = visitedNodes.iterator();
        while(it.hasNext()){
            int nodeId = it.next();
            dist.set(nodeId, Integer.MAX_VALUE);
            parents.set(nodeId, -1);
            if(heuristicSearch)
                heuristic.set(nodeId, -1);
        }
        visitedNodes.clear();
        settledNodes.clear();
        nearestNodes.clear();
        
        it = visitedNodesReverseSearch.iterator();
        while(it.hasNext()){
            int nodeId = it.next();
            distReverseSearch.set(nodeId, Integer.MAX_VALUE);
            parentsReverseSearch.set(nodeId, -1);
        }
        visitedNodesReverseSearch.clear();
        settledNodesReverseSearch.clear();
        nearestNodesReverseSearch.clear();
        //Zavrsetak postavljanja varijabli na pocetne vrijednosti.
        
        //Postavljanje algoritma.
        int N = this.graph.getNumNodes();
        visitedNodes.add(sourceNodeId);
        visitedNodesReverseSearch.add(targetNodeId);
        dist.set(sourceNodeId, 0);
        distReverseSearch.set(targetNodeId, 0);

        nearestNodes.add(new Pair(0, sourceNodeId));
        nearestNodesReverseSearch.add(new Pair(0, targetNodeId));
        //Nije bitna pocetna vrijednost posto se odmah na pocetku pronalazi ponovno najmanji vrh.
        int cvorId = -1;
        //Cuvamo vrijednost najkraceg pronadenog puta u dvosmjernom pretrazivanju.
        int minDist = Integer.MAX_VALUE;
        //Zavrsetak postavljanja algoritma.
        
        //Dvosmjerno Dijsktrino pretrazivanje.
        for(int i = 0; i < 2 * N; ++i){
            //Pronadi sljedeci najblizi posjecen a neobraden vrh.
            //Ukoliko je neki od prioritetnih redova prazan, necemo dalje proanci nove kandidate za rjesenje.
            if(nearestNodes.isEmpty() || nearestNodesReverseSearch.isEmpty())
                return minDist;
            int candidate1 = nearestNodes.first().getValue0();
            int candidate2 = nearestNodesReverseSearch.first().getValue0();
            
            if(candidate1 <= candidate2){
                //Provodimo korak pretrage unaprijed. 
                //Uklanjamo obradeni vrh iz prioritetnog reda.
                cvorId = nearestNodes.pollFirst().getValue1();
                settledNodes.add(cvorId);
                //Ukoliko obradimo u oba smjera neki vrh, tada je pronaden najkraci put.
                if(settledNodesReverseSearch.contains(cvorId))
                    return minDist;
                
                //Pokusaj popraviti rjesenje putom do prostalih vrhova preko vrha cvorId.
                ArrayList<Arc> outNodes = this.graph.outgoingArcs.get(cvorId);
                
                for(Arc arc : outNodes){
                    int headArcNode = arc.anotherEndId;
                    if(!settledNodes.contains(headArcNode) && (dist.get(headArcNode) > dist.get(cvorId) + arc.cost)){
                        //Relaksacija brida (cvorId, headArcNode).
                        nearestNodes.remove(new Pair(dist.get(headArcNode), headArcNode));
                        dist.set(headArcNode, dist.get(cvorId) + arc.cost);
                        nearestNodes.add(new Pair(dist.get(headArcNode), headArcNode));

                        //Provjera jesmo li pronasli kandidata za najkraci put.
                        if(settledNodesReverseSearch.contains(headArcNode) 
                                && (minDist > dist.get(headArcNode) + distReverseSearch.get(headArcNode)))
                            minDist = dist.get(headArcNode) + distReverseSearch.get(headArcNode);
                            
                        if(!visitedNodes.contains(headArcNode))
                            visitedNodes.add(headArcNode);
                        parents.set(headArcNode, cvorId);
                    }
                }
            }
            else{
                //Provodimo korak pretrage unazad.
                //Uklanjamo obradeni vrh iz prioritetnog reda.
                cvorId = nearestNodesReverseSearch.pollFirst().getValue1();
                settledNodesReverseSearch.add(cvorId);
                //Ukoliko obradimo u oba smjera neki vrh, tada je pronaden najkraci put.
                if(settledNodes.contains(cvorId))
                    return minDist;
                
                //Pokusaj popraviti rjesenje putom do prostalih vrhova preko vrha cvorId.
                ArrayList<Arc> inNodes = this.graph.incomingArcs.get(cvorId);
                
                for(Arc arc : inNodes){
                    int headArcNode = arc.anotherEndId;
                    if(!settledNodesReverseSearch.contains(headArcNode) 
                            && (distReverseSearch.get(headArcNode) > distReverseSearch.get(cvorId) + arc.cost)){
                        //Relaksacija brida (cvorId, headArcNode).
                        nearestNodesReverseSearch.remove(new Pair(distReverseSearch.get(headArcNode), headArcNode));
                        distReverseSearch.set(headArcNode, distReverseSearch.get(cvorId) + arc.cost);
                        nearestNodesReverseSearch.add(new Pair(distReverseSearch.get(headArcNode), headArcNode));

                        //Provjera jesmo li pronasli kandidata za najkraci put.
                        if(settledNodes.contains(headArcNode) 
                                && (minDist > distReverseSearch.get(headArcNode) + dist.get(headArcNode)))
                            minDist = distReverseSearch.get(headArcNode) + dist.get(headArcNode);
                            
                        if(!visitedNodesReverseSearch.contains(headArcNode))
                            visitedNodesReverseSearch.add(headArcNode);
                        parentsReverseSearch.set(headArcNode, cvorId);
                    }
                }
            }
        }
        //Vraca MAX_INT ukoliko nije pronaden najkraci put.
        return Integer.MAX_VALUE;
    }
    
    /**
     * Varijacija dvosmjernog dijkstrinog pretrazivanja koja se koristi kod upita 
     * Contraction Hierarchies algoritma.
     * 
     * @param sourceNodeId
     * @param targetNodeId
     * @return Najkraci put izmedu zadanih vrhova. 
     */
    public int computeShortestPathCH(int sourceNodeId, int targetNodeId){
        //Osiguravamo da su zadani vrhovi razliciti.
        if(sourceNodeId == targetNodeId)
            return 0;
        
        //Na pocetku postavi potrebne varijable na pocetne vrijednosti.
        //Postavljamo samo promijenjene vrijednosti, kako ne bi bespotrebno azurirali podatke za svaki vrh.
        //Uzimamo u obzir i pretragu unazad.
        Iterator<Integer> it = visitedNodes.iterator();
        while(it.hasNext()){
            int nodeId = it.next();
            dist.set(nodeId, Integer.MAX_VALUE);
            parents.set(nodeId, -1);
            if(heuristicSearch)
                heuristic.set(nodeId, -1);
        }
        visitedNodes.clear();
        settledNodes.clear();
        nearestNodes.clear();
        
        it = visitedNodesReverseSearch.iterator();
        while(it.hasNext()){
            int nodeId = it.next();
            distReverseSearch.set(nodeId, Integer.MAX_VALUE);
            parentsReverseSearch.set(nodeId, -1);
        }
        visitedNodesReverseSearch.clear();
        settledNodesReverseSearch.clear();
        nearestNodesReverseSearch.clear();
        //Zavrsetak postavljanja varijabli na pocetne vrijednosti.
        
        //Postavljanje algoritma.
        int N = this.graph.getNumNodes();
        visitedNodes.add(sourceNodeId);
        visitedNodesReverseSearch.add(targetNodeId);
        dist.set(sourceNodeId, 0);
        distReverseSearch.set(targetNodeId, 0);

        nearestNodes.add(new Pair(0, sourceNodeId));
        nearestNodesReverseSearch.add(new Pair(0, targetNodeId));
        //Nije bitna pocetna vrijednost posto se odmah na pocetku pronalazi ponovno najmanji vrh.
        int cvorId = -1;
        //Cuvamo vrijednost najkraceg pronadenog puta u dvosmjernom pretrazivanju.
        int minDist = Integer.MAX_VALUE;
        //Zavrsetak postavljanja algoritma.
        
        //Dvosmjerno Dijsktrino pretrazivanje.
        for(int i = 0; i < 2 * N; ++i){
            //Pronadi sljedeci najblizi posjecen a neobraden vrh.
            int candidate1 = Integer.MAX_VALUE;
            if(!nearestNodes.isEmpty() && (nearestNodes.first().getValue0() < minDist))
                candidate1 = nearestNodes.first().getValue0();
            int candidate2 = Integer.MAX_VALUE;
            if(!nearestNodesReverseSearch.isEmpty() && (nearestNodesReverseSearch.first().getValue0() < minDist))
                candidate2 = nearestNodesReverseSearch.first().getValue0();
            
            //Ukoliko je najmanja vrijednost u oba prioritetna reda veca od najkraceg pronadenog puta,
            //tada mozemo zaustaviti pretragu. Daljnjim pretrazivanjem sigurno necemo pronaci kraci put.
            if(Math.min(candidate1, candidate2) == Integer.MAX_VALUE)
                return minDist;
            
            if(candidate1 <= candidate2){
                //Provodimo korak pretrage unaprijed. 
                //Micemo obradeni vrh iz prioritetnog reda.
                cvorId = nearestNodes.pollFirst().getValue1();
                settledNodes.add(cvorId);
                //Ukoliko obradimo u oba smjera neki vrh, dobili smo novog kandidata za rjesenje.
                if(settledNodesReverseSearch.contains(cvorId))
                    if(dist.get(cvorId) + distReverseSearch.get(cvorId) < minDist)
                        minDist = dist.get(cvorId) + distReverseSearch.get(cvorId);
                
                //Stall-on-demand
                boolean stall = false;
                ArrayList<Arc> inNodes = this.graph.incomingArcs.get(cvorId);
                for(Arc arc : inNodes){
                    if(arc.arcFlag){
                        if(dist.get(arc.anotherEndId) != Integer.MAX_VALUE && (dist.get(arc.anotherEndId) + arc.cost < dist.get(cvorId))){
                            //Ne treba dalje pretrazivati posto nece ovim putem biti pronaden najkraci put.
                            stall = true;
                            break;
                        }
                    }
                }
                if(stall)
                    continue;
                
                //Pokusaj popraviti rjesenje putom do prostalih vrhova preko vrha cvorId.
                ArrayList<Arc> outNodes = this.graph.outgoingArcs.get(cvorId);
                
                for(Arc arc : outNodes){
                    //Pretrazujemo samo uzlazne bridove.
                    if(!arc.arcFlag){
                        continue;
                    }
                    
                    int headArcNode = arc.anotherEndId;
                    if(!settledNodes.contains(headArcNode) && (dist.get(headArcNode) > dist.get(cvorId) + arc.cost)){
                        //Relaksacija brida (cvorId, headArcNode).
                        nearestNodes.remove(new Pair(dist.get(headArcNode), headArcNode));
                        dist.set(headArcNode, dist.get(cvorId) + arc.cost);
                        nearestNodes.add(new Pair(dist.get(headArcNode), headArcNode));
                            
                        if(!visitedNodes.contains(headArcNode))
                            visitedNodes.add(headArcNode);
                        parents.set(headArcNode, cvorId);
                    }
                }
            }
            else{
                //Provodimo korak pretrage unazad.
                //Micemo obradeni vrh iz prioritetnog reda.
                cvorId = nearestNodesReverseSearch.pollFirst().getValue1();
                settledNodesReverseSearch.add(cvorId);
                //Ukoliko obradimo u oba smjera neki vrh, dobili smo novog kandidata za rjesenje.
                if(settledNodes.contains(cvorId))
                    if(dist.get(cvorId) + distReverseSearch.get(cvorId) < minDist)
                        minDist = dist.get(cvorId) + distReverseSearch.get(cvorId);
                
                //Stall-on-demand
                boolean stall = false;
                ArrayList<Arc> outNodes = this.graph.outgoingArcs.get(cvorId);
                for(Arc arc : outNodes){
                    if(arc.arcFlag){
                        if(distReverseSearch.get(arc.anotherEndId) != Integer.MAX_VALUE 
                                && (distReverseSearch.get(arc.anotherEndId) + arc.cost < distReverseSearch.get(cvorId))){
                            //Ne treba dalje pretrazivati posto nece ovim putem biti pronaden najkraci put.
                            stall = true;
                            break;
                        }
                    }
                }
                if(stall)
                    continue;
                
                //Pokusaj popraviti rjesenje putom do prostalih vrhova preko vrha cvorId.
                ArrayList<Arc> inNodes = this.graph.incomingArcs.get(cvorId);
                
                for(Arc arc : inNodes){
                    //Pretrazujemo samo uzlazne bridove.
                    if(!arc.arcFlag){
                        continue;
                    }
                    
                    int headArcNode = arc.anotherEndId;
                    if(!settledNodesReverseSearch.contains(headArcNode) 
                            && (distReverseSearch.get(headArcNode) > distReverseSearch.get(cvorId) + arc.cost)){
                        //Relaksacija brida (cvorId, headArcNode).
                        nearestNodesReverseSearch.remove(new Pair(distReverseSearch.get(headArcNode), headArcNode));
                        distReverseSearch.set(headArcNode, distReverseSearch.get(cvorId) + arc.cost);
                        nearestNodesReverseSearch.add(new Pair(distReverseSearch.get(headArcNode), headArcNode));

                        if(!visitedNodesReverseSearch.contains(headArcNode))
                            visitedNodesReverseSearch.add(headArcNode);
                        parentsReverseSearch.set(headArcNode, cvorId);
                    }
                }
            }
        }
        //Vraca MAX_INT ukoliko nije pronaden najkraci put.
        return Integer.MAX_VALUE;
    }
    
    
    /**
     * Metoda koja vraca string zapis izracunatog najkraceg puta u grafu.
     * Prethodno je potrebno pozvati computeShortestPath(u,v) cime je pronaden
     * takav put. Rekonstruira se iz parents polja.
     */
    public String getCalculatedShorthestPath(int targetNodeId){
        ArrayList<Integer> reversePath = new ArrayList<>();
        reversePath.add(targetNodeId);
        int prnt = parents.get(targetNodeId);
        while(prnt != -1){
            reversePath.add(prnt);
            prnt = parents.get(prnt);
        }
        
        StringBuilder sb = new StringBuilder("[" + reversePath.get(reversePath.size() - 1));
        for(int i = reversePath.size() - 2; i >= 0; i--)
            sb.append("," + reversePath.get(i));
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Metoda koja vraca broj vrhova na najkracem putu.
     * Prethodno je potrebno pozvati camputeShortestPath metodu.
     * @param targetNodeId
     * @return broj vrhova na najkracem putu
     */
    public int nodesOnShortestPath(int targetNodeId){
        ArrayList<Integer> reversePath = new ArrayList<>();
        reversePath.add(targetNodeId);
        int prnt = parents.get(targetNodeId);
        while(prnt != -1){
            reversePath.add(prnt);
            prnt = parents.get(prnt);
        }
        return reversePath.size();
    }
    
    @Override
    public int getNumberOfSettledNodes(){
        return settledNodes.size() + settledNodesReverseSearch.size();
    }
    
    @Override
    public int getNumberOfNodes(){
        return graph.getNumNodes();
    }
    
    @Override
    public int getNumberOfEdges(){
        return graph.getNumEdges();
    }
    
    public void setHeuristicSearch(){
        this.heuristicSearch = true;
    }
    
    public void unsetHeuristicSearch(){
        this.heuristicSearch = false;
    }
    
    public void setReverseSearch(){
        reverseSearch = true;
    }
    
    public void unsetReverseSearch(){
        reverseSearch = false;
    }
    
    //Sluzi da racunanje heuristicke funkcije prilikom ALT pretrazivanja.
    public void setLandmarkAlgorithm(LandmarkAlgorithm alt){
        this.altAlgorithm = alt;
    }
    
    //Sluzi za dohvacanje rank vrijednosti.
    public void setContractionHierarchiesAlgorithm(ContractionHierarchies ch){
        this.chAlgorithm = ch;
    }
    
    //Ne radi nista, nije potrebno predprocesiranje.
    @Override
    public void preprocess(int numLandmarks, String option){
    }
}
