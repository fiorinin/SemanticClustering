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
package com.ema.semanticclustering.io;

import com.ema.semanticclustering.model.Bookmark;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import structure.Entity;

/**
 * Class used to load the dataset.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class DataLoader {

    static Logger logger = LoggerFactory.getLogger(DataLoader.class);

    /**
     * Load the Bookmarks from the given bookmark file.
     *
     * @param bookmarks_info_file which contains user description
     * @return a map key: bookmark URI, value: bookmark
     * @throws Exception
     */
    public static Map<String, Entity> loadBookmarks(String bookmarks_info_file) throws Exception {

        logger.info("Loading bookmarks from " + bookmarks_info_file);
        Map<String, Entity> bookmarks = new HashMap();
        for (Map.Entry<URI, Set<URI>> e : loadDataFile(bookmarks_info_file).entrySet()) {

            logger.info("Loading bookmark: " + e.getKey() + "\t" + e.getValue().size() + " annotations");
            Entity b = new Entity();
            b.setId(e.getKey().getLocalName());
            b.setConcepts(e.getValue());
            bookmarks.put(b.getId(), b);
        }
        logger.info(bookmarks.size() + " bookmarks loaded");

        return bookmarks;
    }

    /**
     * Generic loader
     *
     * @param file
     * @return
     * @throws Exception
     */
    private static Map<URI, Set<URI>> loadDataFile(String file) throws Exception {

        URIFactory factory = URIFactoryMemory.getSingleton();
        Map<URI, Set<URI>> uriMap = new HashMap();

        BufferedReader br = new BufferedReader(new FileReader(file));
        try {
            String line = br.readLine();

            while (line != null) {

                String[] data = line.split("\t", 2);
                if (data.length == 2) {
                    String[] data2 = data[1].split(",");

                    Set<URI> uris = new HashSet();
                    for (String u : data2) {
                        uris.add(factory.getURI(u));
                    }

                    URI targetUri = factory.getURI(data[0]);
                    uriMap.put(targetUri, uris);

                } else {
                    logger.error("Do not consider malformed line '" + line + "' into " + file);
                }

                line = br.readLine();
            }
        } finally {
            br.close();
        }
        return uriMap;
    }

}
