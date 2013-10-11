package org.abatons.markov.graph;


public interface GraphPersistence {
	Graph load();
	void save(Graph inGraph);
}
