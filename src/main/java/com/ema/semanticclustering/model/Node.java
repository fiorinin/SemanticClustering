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
package com.ema.semanticclustering.model;

import java.util.ArrayList;
import java.util.List;
import org.openrdf.model.URI;
import structure.Entity;

/**
 *
 * @author Nicolas Fiorini <nicolas.fiorini@mines-ales.fr> /
 * <contact@creatox.com>
 */
public class Node {

    private Entity data;
    private Node parent;
    private List<Node> children = new ArrayList();
    private double distanceToParent;

    public double getDistanceToParent() {
        return distanceToParent;
    }

    public void setDistanceToParent(double distanceToParent) {
        this.distanceToParent = distanceToParent;
    }

    public Entity getData() {
        return data;
    }

    public void setData(Entity data) {
        this.data = data;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public void removeChild(Node child) {
        this.children.remove(child);
    }

    public void addChild(Node child) {
        this.children.add(child);
    }

    public Node(Entity d) {
        data = d;
    }

    public void print() {
        print("", true);
    }
    
    private void print(String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + data.getId());
        for (int i = 0; i < children.size() - 1; i++) {
            children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
        }
        if (children.size() > 0) {
            children.get(children.size() - 1).print(prefix + (isTail ?"    " : "│   "), true);
        }
    }
    
    public static String toNewick(Node root, boolean supp) {
        if (root.getChildren().size() > 0) {
            String output = "";
            output += "(";
            for(int k=0; k<root.getChildren().size();k++) {
                output += toNewick(root.getChildren().get(k),supp);
                if(k != root.getChildren().size()-1) output += ",";
            }
            String concepts = "";
//            for(URI u : root.data.getConcepts()) {
//                concepts+= (concepts.equals("") ? "" : "_")+u.getLocalName().replace("synset-", "").replace("-noun", "");
//            }
            if(supp)
                output += ")"+concepts+":"+root.distanceToParent;
            else
                output += ")";
            return output;
        } else {
            if(supp)
                return root.getData().getId()+":"+root.distanceToParent;
            else
                return "D_"+root.getData().getId();
        }
    }
    
    public static String toJS(Node root) {
        String conceptList = root.getData().getConcepts().toString();
        String concepts = conceptList.replace("[", "").replace("]","").replace("http://purl.org/vocabularies/princeton/wn30/synset-","").replace(",","");
        String nodeID = root.getData().getId();
        boolean cl = false;
        if(!root.getData().getId().equals("Clusters")) {
            cl = true;
            String[] split = concepts.split(" ");
            nodeID = "";
            for(String s : split) {
                String[] chunk = s.split("-");
                for(int j=0; j<chunk.length-2; j++) {
                    nodeID += chunk[j]+" ";
                }
            }
        }
        String nid = "";
        if(!cl) {
            nid = "'id':'"+nodeID+"', ";
        }
        String str = "{"+nid+"'text': '"+nodeID+"', 'concepts': '"+concepts+"', 'top': '"+root.getDistanceToParent()+"'";
        if(root.getChildren().isEmpty()) {
            str += ", 'icon' : 'jstree-file', 'children' : []}";
        } else {
            str += ", 'state': {'opened': true}, 'children' : [";
            int k = 1;
            for(Node c : root.getChildren()) {
                String r = toJS(c);
                str += r+(k == root.getChildren().size() ? "" : ",");
                k++;
            }
            str += "]}";
        }
        return str;
    }
}
