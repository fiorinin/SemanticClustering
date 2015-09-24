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
package com.ema.semanticclustering;

import com.ema.semanticclustering.model.Node;
import com.ema.semanticclustering.model.Tree;
import com.ema.semanticclustering.processing.Clustering;
import com.ema.semanticclustering.io.DataLoader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import managers.EngineOverlay;
import managers.EntityLabelizer;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.algo.utils.GAction;
import slib.graph.algo.utils.GActionType;
import slib.graph.algo.utils.GraphActionExecutor;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.loader.GraphLoaderGeneric;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.graph.model.impl.graph.memory.GraphMemory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.indexer.IndexHash;
import structure.Entity;
import structure.Index;
import tools.Utils;

/**
 *
 * @author Nicolas Fiorini <nicolas.fiorini@mines-ales.fr> /
 * <contact@creatox.com>
 */
public class Main {

    static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        // Conf
        String root = System.getProperty("user.dir") + "/src/main/resources/";
        String root_knowledge = root + "/knowledge/";
        String wordnetRDF = root_knowledge + "/wordnet3_0_xp_annot.nt";

        // List of all datasets of the benchmark in the resources folder
        ArrayList<String> datasets = new ArrayList();
        datasets.add(root + "eval1/bookmarks.csv");
        datasets.add(root + "eval2/bookmarks.csv");
        datasets.add(root + "eval3/bookmarks.csv");
        datasets.add(root + "eval4/bookmarks.csv");
        datasets.add(root + "eval5/bookmarks.csv");
        datasets.add(root + "eval6/bookmarks.csv");
        datasets.add(root + "eval7/bookmarks.csv");
        
        /*
         *  The strategies to use:
         *    1: corresponds to HSC
         *    2: corresponds to LSC
         *    3: corresponds to baseline (classical HAC)
         */
        ArrayList<Integer> strategies = new ArrayList();
//        strategies.add(1);
        strategies.add(2);
//        strategies.add(3);
        
        /*
         *  The configurations to use:
         *    1: corresponds to full
         *    2: corresponds to half
         *    3: corresponds to none
         */
        ArrayList<Integer> configs = new ArrayList();
        configs.add(1);
//        configs.add(2);
//        configs.add(3);

        for (String bookmarks_info : datasets) {

            // Load the bookmarks as defined by knowdive dataset
            Map<String, Entity> bookmarkIndex = DataLoader.loadBookmarks(bookmarks_info);

            // Retrieve all concepts used to characterize the bookmarks
            Set<URI> concepts = new HashSet();
            for (Entity b : bookmarkIndex.values()) {
                concepts.addAll(b.getConcepts());
            }

            // Checking that all bookmarks are associated with concepts
            int bookmarksWithoutConcepts = 0;
            for (Entity b : bookmarkIndex.values()) {
                if (b.getConcepts().isEmpty()) {
                    logger.info("<!!!> bookmark " + b.getId() + " does not have associated concepts");
                    bookmarksWithoutConcepts++;
                }
            }
            logger.info(bookmarksWithoutConcepts + " bookmarks do not have concepts ");

            // We now focus on WordNet
            URIFactory factory = URIFactoryMemory.getSingleton();
            URI guri = factory.getURI("http://graph/wordnet/");
            G graph = new GraphMemory(guri);

            // We load the data into the graph
            GDataConf wordnetRDFconf = new GDataConf(GFormat.NTRIPLES, wordnetRDF);
            GraphLoaderGeneric.populate(wordnetRDFconf, graph);

            // We replace hyponyms relationships by subclassof in order to be able to process WordNet using SML
            GAction substituteHyponym = new GAction(GActionType.PREDICATE_SUBSTITUTE);
            substituteHyponym.addParameter("old_uri", "http://www.w3.org/2006/03/wn/wn20/schema/hyponymOf");
            substituteHyponym.addParameter("new_uri", "RDFS.SUBCLASSOF");
            GraphActionExecutor.applyAction(substituteHyponym, graph);

        // Instances will also be considered as classes - this also for SML compliance
            // no effect on similarity computation
            GAction substituteInstanceOf = new GAction(GActionType.PREDICATE_SUBSTITUTE);
            substituteInstanceOf.addParameter("old_uri", "http://purl.org/vocabularies/princeton/wordnet/schema#instanceOf");
            substituteInstanceOf.addParameter("new_uri", "RDFS.SUBCLASSOF");
            GraphActionExecutor.applyAction(substituteInstanceOf, graph);
            logger.info("SubClassOf: " + graph.getE(RDFS.SUBCLASSOF).size());

        // We root the graph which has been loaded (this is optional but may be required 
            // to compare synset which do not share common ancestors).
            GAction addRoot = new GAction(GActionType.REROOTING);
            GraphActionExecutor.applyAction(addRoot, graph);

        // We check if the cleaning as been correctly made 
            // and if all URIs considered are defined in the WordNet version considered
            int notFoundURI = 0;
            Set<URI> uriCollection = new HashSet();
            Set<Entity> notClusterizable = new HashSet();
            for (Entity b : bookmarkIndex.values()) {
                HashSet<URI> bConcepts = new HashSet<>(b.getConcepts());
                for (URI uri : bConcepts) {
                    if (!uriCollection.contains(uri) && !graph.containsVertex(uri)) {
                        logger.info("Graph does not contains URI: " + uri);
                        notFoundURI++;
                    }
                    if (!graph.containsVertex(uri)) {
                        b.removeConcept(uri);
                    }
                    uriCollection.add(uri);
                }
                if (b.getConcepts().isEmpty()) {
                    notClusterizable.add(b);
                }
            }
            // Stop if any bookmark can't be clustered after removing URIs that aren't found
            if (!notClusterizable.isEmpty()) {
                System.exit(0);
            }
            logger.info("URIs not found: " + notFoundURI + "/" + uriCollection.size());
            logger.info("Those URIs have been removed from annotations.");
            
            // USI Index initialization
            Index i = Index.getInstance();
            EngineOverlay EO = new EngineOverlay(graph, new IndexHash(), false);
            i.init(EO);

            // USI set up
            EntityLabelizer el = new EntityLabelizer(0, 0.075);
            el.setFilter(0);
            el.setMap(false);
            el.setIncludeAncestors(true);
            el.setClustering(true);
            
            // Run the clustering algorithm for each strategy and for each config
            for(int strategy : strategies) {
                for(int config : configs) {
                    // Cluster initialization
                    Clustering clustering = new Clustering(new ArrayList(bookmarkIndex.values()), el, strategy);

                    // Run the clustering
                    Tree tree = clustering.runClustering();

                    // Now focus on the quantiles
                    Percentile pct = new Percentile();
                    // Fetching branch lengths and ordering them
                    ArrayList<Double> distList = clustering.getDistArr();
                    double[] dists = new double[distList.size()];
                    for (int k = 0; k < dists.length; k++) {
                        dists[k] = distList.get(k);
                    }
                    Arrays.sort(dists);
                    // Define the two quantiles
                    double q1 = pct.evaluate(dists, 35);
                    double q2 = pct.evaluate(dists, 60);

                    // Post-process
                    tree.processTree(q1, q2, config);
                    
                    // Prepare the file name
                    String outputname = "";
                    if(strategy == 1)
                        outputname += "HSC-";
                    else if(strategy == 2)
                        outputname += "LSC-";
                    else
                        outputname += "baseline-";
                    if(config == 1)
                        outputname += "full";
                    else if(config == 2)
                        outputname += "half";
                    else
                        outputname += "none";
                    outputname += ".nwk";
                    System.out.println(outputname+" has been computed.");

                    // Output the newick tree
                    String newick = Node.toNewick(tree.getRoot(), false);
                    try (PrintWriter writer = new PrintWriter(bookmarks_info.replace("bookmarks.csv", outputname), "UTF-8")) {
                        writer.print(newick + ";");
                        writer.close();
                    }
                }
            }
        }
    }
}
