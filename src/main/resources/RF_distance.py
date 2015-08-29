from ete2 import Tree
import sys, os.path

def compare(f1,f2):
	# Load tree 1
	t1n = f1
	with open(t1n) as f:
	    t1s = f.read()
	t1 = Tree(t1s)

	# Load tree 2
	t2n = f2
	with open(t2n) as f:
	    t2s = f.read()
	t2 = Tree(t2s)

	rf, rf_max, common_attrs, names, edges_t1, edges_t2, discarded_edges_t1 = t1.robinson_foulds(t2,unrooted_trees=True)

	# At most there are 2*leaves-3 elementary changes to transform t1 into t2
	leaves = len(t2)
	maxnodes = 2*leaves-3
	return float(rf)/maxnodes

strategies = ["HSC-","LSC-","baseline-"]
configs = ["full","half","none"]

for strategy in strategies:
	for config in configs:
		for j in range(1,5):
			for i in range(1,8):
				if os.path.isfile("eval"+str(i)+"/expert"+str(j)+".nwk"):
					print compare("eval"+str(i)+"/"+strategy+config+".nwk", "eval"+str(i)+"/expert"+str(j)+".nwk"),
				print "\t",
			print "\n",
		print "\n\n",


