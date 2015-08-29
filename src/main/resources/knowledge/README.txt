This benchmark relies on data from a previous study [1] that associated bookmarks to WordNet synsets.
In fact, this association relies on Knowdive [2], so we had to (i) get the data from knowdive, (ii)
map, when possible, the knowdive URIs to WordNet URIs (provided the closeMatch relationship) and
(iii) get a dump of WordNet 3.0, which is actually hard to find.

Therefore we provide several files so that you can use this benchmark:

- wordnet3_0_xp_annot.nt: this is the WordNet 3.0 dump. It is a ntriples file of WordNet graph,
  thus under the form <subject> <predicate> <object> ., with one triplet per line. It does not contain 
  triplets associated to the verbs (see the last item of the list). Basically the RDF has been built
  by crawling the Web for WordNet URI descriptions. Next we removed all the triplets associated to 
  verbs and we changed hyponym and instanceOf relationships.

- mapping_knowdive_wordnet.csv: this is the mapping between the Knowdive synset URIs (in [2]) and the 
  corresponding WordNet URI. The mapping has been done automatically by fetching the closeMatch
  relationship provided in the Knowdive dataset, which gives the corresponding WordNet synset.
  However, some concepts do not have any mapping [3]. Each line contains the Knowdive URI and
  the corresponding WordNet URI.

- mapping_notfound.csv: some mapping were not up to date, so we had to manually map them
  to WordNet synsets. This file contains the manual mapping that we did. The format is the same
  than for the previous file, except that some Knowdive URIs have no corresponding WordNet URI.
  In those cases, the WordNet URI is replaced by 'DELETE'. Note that the mappings override 
  Knowdive mappings (in mapping_knowdive_wordnet.csv). This is mainly to avoid mapping to adjectives 
  and verbs since (i) adjectives are not structured into a taxonomy and (ii) the structure of verb 
  in WordNet does not form a DAG.

Normally, you just have to use the first file since all bookmarks are already annotated by WordNet
URIs, and not by the Knowdive ones. These details only help you understanding the creation of this
benchmark. 


[1] Andrews, Pierre and Pane, Juan and Zaihrayeu, Ilya (2011) Semantic Disambiguation in Folksonomy: a Case Study; in Advanced Language Technologies for Digital Libraries, Springer's Lecture Notes on Computer Science (LNCS) Hot Topic subline, Vol 6699; pp 114-134, DOI: 10.1007/978-3-642-23160-5_8. (bibtex,mendeley)
[2] http://disi.unitn.it/~knowdive/dataset/delicious/
[3] 21 concepts do not have mappings (closeMatch relationship): 
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/grammar-NOUN-272650
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/fabricated-ADJECTIVE-287435
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/amusing-ADJECTIVE-272179
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/photograph-NOUN-278070
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/video_recording-NOUN-286653
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/news-NOUN-275794
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/cake-NOUN-235703
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/reading-NOUN-234119
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/daily-ADJECTIVE-310139
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/food-NOUN-235559
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/exchange-NOUN-234113
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/Japan-NOUN-285880
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/Vietnamese-ADJECTIVE-306533
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/child-NOUN-239396
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/Ph_D_-NOUN-340921
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/ethical-ADJECTIVE-340210
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/aid-NOUN-306129
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/conservative-ADJECTIVE-297646
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/collaborator-NOUN-292285
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/press-NOUN-234816
	http://disi.unitn.it/~knowdive/dataset/delicious/concept/Africa-NOUN-251176