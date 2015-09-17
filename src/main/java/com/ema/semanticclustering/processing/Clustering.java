/*
 *  Copyright or © or Copr. Ecole des Mines d'Alès (2012-2014) 
 *  
 *  This software is a computer program whose purpose is to provide 
 *  several functionalities for the processing of semantic data 
 *  sources such as ontologies or text corpora.
 *  
 *  This software is governed by the CeCILL  license under French law and
 *  abiding by the rules of distribution of free software.  You can  use, 
 *  modify and/ or redistribute the software under the terms of the CeCILL
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info". 
 * 
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability. 

 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or 
 *  data to be ensured and,  more generally, to use and operate it in the 
 *  same conditions as regards security. 
 * 
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL license and that you accept its terms.
 */

package com.ema.semanticclustering.processing;

import com.ema.semanticclustering.model.Node;
import com.ema.semanticclustering.model.Tree;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import managers.EntityLabelizer;
import org.openrdf.model.URI;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import structure.ConcentricClusterSet;
import structure.Entity;
import structure.Index;
import structure.Point;
import structure.PointDist;

/**
 *
 * @author Nicolas Fiorini <nicolas.fiorini@mines-ales.fr> /
 * <contact@creatox.com>
 */
public class Clustering {
    private List<List<Double>> clusterMatrix;
    private Tree tree;
    private ArrayList<Node> nodes;
    private EntityLabelizer el;
    private ArrayList<Double> dists;
    private int strategy;
    
    /*
      Dirty patch to simulate an optimized classical HAC in terms
      of execution time. Skip stores the time spent on computing
      SSs with no reason because the matrix hasn't been designed
      for classical HAC. Use skip value to get the real processing
      time.
    */
    public long skip = 0; 
    
    public Clustering(ArrayList<Entity> en, EntityLabelizer enlab, int strat) {
        el = enlab;
        nodes = new ArrayList();
        dists = new ArrayList();
        strategy = strat;
        for(Entity e : en) {
            nodes.add(new Node(e));
        }
        init();
    }
    
    public String iterateDist(Node n) {
        String iter = (n.equals(tree.getRoot()) ? "" : ",") + n.getDistanceToParent();
        for(Node c : n.getChildren()) {
            iter += iterateDist(c);
        }
        return iter;
    }
    
    public void iterateDistArr(Node n) {
        dists.add(n.getDistanceToParent());
        for(Node c : n.getChildren()) {
            iterateDistArr(c);
        }
    }
    
    public String getDistStr() {
        return "d <- c(" + iterateDist(tree.getRoot()) + ")";
    }
    
    public ArrayList<Double> getDistArr() {
        iterateDistArr(tree.getRoot());
        return dists;
    }
    
    private void init() {
        clusterMatrix = new ArrayList(nodes.size());
        for (int j = 0; j < nodes.size(); j++) {
            Set<URI> bookmarks_i = nodes.get(j).getData().getConcepts();
            ArrayList<Double> dists = new ArrayList();
            for (int k = j+1; k < nodes.size(); k++) {
                Set<URI> bookmarks_j = nodes.get(k).getData().getConcepts();
                double dist = 1-Index.getInstance().getEngineManager().calculateGroupwise(bookmarks_i, bookmarks_j);
                dists.add(dist);
            }
            clusterMatrix.add(dists);
        }
    }
    
    private void addNode(Node toAdd) {
        // Compute the new column
        ArrayList<Double> dists = new ArrayList();
        for(int k=0; k<nodes.size(); k++) {
            if(strategy != 3)
                dists.add(1-(Index.getInstance().getEngineManager().calculateGroupwise(nodes.get(k).getData().getConcepts(), toAdd.getData().getConcepts())));
            else {
                // Classical clustering : fetching all cluster elements
                ArrayList<Node> c1 = new ArrayList();
                ArrayList<Node> c2 = new ArrayList();
                c1.addAll(getAllChildren(nodes.get(k)));
                c2.addAll(getAllChildren(toAdd));
                double dist = 0;
                // Single linkage (min distances)
                // Complete linkage (max distances)
                // WPGMA (average distance)
                for(Node ci:c1) {
                    for(Node cj : c2) {
                        long start = System.currentTimeMillis();
                        dist += 1-(Index.getInstance().getEngineManager().calculateGroupwise(ci.getData().getConcepts(), cj.getData().getConcepts()));
                        skip += System.currentTimeMillis()-start;
                    }
                }
                dist = dist/(c1.size()*c2.size());
                // Now add in the list for the matrix
                dists.add(dist);
            }
        }
        // Update all columns with the one computed
        for(int i = 0; i< dists.size(); i++) {
            clusterMatrix.get(i).add(dists.get(i));
        }
        // Update lines
        clusterMatrix.add(new ArrayList());
        nodes.add(toAdd);
    }
    
    private void removeNode(int n) {
        // Remove the row
        clusterMatrix.remove(n);
        
        // And the column
        for(int k = 0; k < n; k++) {
            List<Double> arr = clusterMatrix.get(k);
            arr.remove(n-(k+1));
        }
        nodes.remove(n);
    }
    
    public List<Node> getAllChildren(Node n) {
        List<Node> children = new ArrayList();
        if(!n.getChildren().isEmpty()) {
            for(Node c : n.getChildren()) {
                children.addAll(getAllChildren(c));
            }
        } else {
            children.add(n);
        }
        return children;
    }
    
    private void iterate() throws SLIB_Exception {
        double min = 1;
        Node n1 = new Node(null);
        Node n2 = new Node(null);
        for (int j = 0; j < clusterMatrix.size(); j++) {
            for (int k = 0; k < clusterMatrix.get(j).size(); k++) {
                if(clusterMatrix.get(j).get(k) < min) {
                    min = clusterMatrix.get(j).get(k);
                    n1 = nodes.get(j);
                    n2 = nodes.get(j+k+1);
                }
            }
        }
        // Create the node
        ArrayList<Node> toCluster = new ArrayList();
        // Here we only add the children as the neighbours (strategy 2)
        if(strategy == 2) {
            toCluster.add(n1);
            toCluster.add(n2);
        }
        // But we can also add all their children! (strategy 1 or 3)
        // Because we want to compare classical HAC with proper node
        // labels, otherwise the post-process will make it as good
        // as LSC, or worse.
        else {
            toCluster.addAll(getAllChildren(n1));
            toCluster.addAll(getAllChildren(n2));
        }
        Node newNode = computeNode(toCluster);
        
        // Update the tree and the matrix
        n1.setParent(newNode);
        double distn1 = min;
        double distn2 = min;
        
        // Only classical HAC doesn't use semantic similarities for branch lenths
        if(strategy != 3) {
            distn1 = 1-(Index.getInstance().getEngineManager().calculateGroupwise(newNode.getData().getConcepts(), n1.getData().getConcepts()));
            distn2 = 1-(Index.getInstance().getEngineManager().calculateGroupwise(newNode.getData().getConcepts(), n2.getData().getConcepts()));
        }
        n1.setDistanceToParent(distn1);
        n2.setParent(newNode);
        n2.setDistanceToParent(distn2);
        newNode.addChild(n1);
        newNode.addChild(n2);
        addNode(newNode);
        removeNode(nodes.indexOf(n1));
        removeNode(nodes.indexOf(n2));
    }
    
    private Node computeNode(ArrayList<Node> inCluster) throws SLIB_Ex_Critic, SLIB_Exception {
        // Node creation
        Entity agreg = new Entity();
        agreg.setId("");
        
        // Fill the index and the cluster
        ConcentricClusterSet ccs = ConcentricClusterSet.getInstance();
        
        // Can be optimized here: the index is repeatedly cleared/filled
        Index i = Index.getInstance();
        i.clear();
        ArrayList<PointDist> points = new ArrayList();
        Set<URI> firstSuggestion = new HashSet();
        for (Node e : inCluster) {
            agreg.setId((agreg.getId().equals("") ? "" : agreg.getId()+"|")+e.getData().getId());
            i.addEntity(e.getData());
            points.add(new PointDist(new Point(0, 0, e.getData().getId()),0));
        }
        ccs.setPoints(points);
        
        // Annotate the node
        el.setNeighboursNumberMin(inCluster.size());
        el.prepare();

        firstSuggestion = el.labelize();
        agreg.setConcepts(firstSuggestion);
        return new Node(agreg);
    }
    
    public Tree runClustering() throws SLIB_Exception {
        while(nodes.size() > 1) {
            iterate();
        }
        Entity e = new Entity();
        e.setId("Clusters");
        Node root = new Node(e);
        root.addChild(nodes.get(0));
        nodes.get(0).setParent(root);
        nodes.get(0).setDistanceToParent(0.35);
        tree = new Tree(root);
        return tree;
    }
    
    public void printTree() {
        nodes.get(0).print();
    }
}
