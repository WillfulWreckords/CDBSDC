package com.wwrkds.datamodels;

/**
 * Enumeration for dealing with ranking order
 * 
 * @author jonlareau
 * 
 */
public enum RankingOrder {
	ASCENDING(1), DESCENDING(-1);
	private int value = 0;

	RankingOrder(int i) {
		this.value = i;
	}

	public int getDirection() {
		return this.value;
	}
}