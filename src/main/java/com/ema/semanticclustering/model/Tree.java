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
import java.util.HashSet;

/**
 *
 * @author Nicolas Fiorini <nicolas.fiorini@mines-ales.fr> /
 * <contact@creatox.com>
 */

public class Tree {
    private Node root;
    private double sth;
    private double hth;
    private int config;

    public Node getRoot() {
        return root;
    }

    public Tree(Node n) {
        root = n;
    }
    
    public void processTree(double sth, double hth, int conf) {
        this.sth = sth;
        this.hth = hth;
        config = conf;
        // Disable for none config
        if(config == 3)
            return;
        recursiveProcess(root);
    }
    
    private void recursiveProcess(Node n) {
        if(!n.getChildren().isEmpty()) {
            ArrayList<Node> newChildren = new ArrayList();
            HashSet<Node> rmChildren = new HashSet();
            for(Node c : n.getChildren()) {
                recursiveProcess(c);
                if(!c.getChildren().isEmpty() && (c.getDistanceToParent() <= sth || c.getData().getConcepts().equals(n.getData().getConcepts()))) {
                    int proportion = 0;
                    ArrayList<Node> localrm = new ArrayList();
                    for(Node cc : c.getChildren()) {
                        if(cc.getDistanceToParent() <= sth || c.getData().getConcepts().equals(n.getData().getConcepts())) {
                            cc.setParent(n);
                            newChildren.add(cc);
                            localrm.add(cc);
                            proportion++;
                        }
                    }
                    if(proportion == c.getChildren().size()) {
                        rmChildren.add(c);
                    }
                    for(Node cc : localrm) {
                        c.removeChild(cc);
                    }
                }
                // Disable for half config
                if(!c.getChildren().isEmpty() && c.getDistanceToParent() >= hth && config == 1) {
                    int proportion = 0;
                    ArrayList<Node> localrm = new ArrayList();
                    for(Node cc : c.getChildren()) {
                        if(cc.getDistanceToParent() >= hth) {
                            cc.setParent(n);
                            newChildren.add(cc);
                            localrm.add(cc);
                            proportion++;
                        }
                    }
                    if(proportion == c.getChildren().size()) {
                        rmChildren.add(c);
                    }
                    for(Node cc : localrm) {
                        c.removeChild(cc);
                    }
                }
            }
            for(Node c : newChildren) {
                n.addChild(c);
            }
            for(Node c : rmChildren) {
                n.removeChild(c);
            }
        }
    }
}