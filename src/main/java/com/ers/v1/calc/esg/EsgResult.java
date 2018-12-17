/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.calc.esg;

import java.util.Map;

/**
 *
 * @author snyanakieva
 */
public class EsgResult {

	private Map<EsgObject, Double> positiveWeighted;
	private Map<EsgObject, Double> negativeWeighted;
	private int e;
	private int s;
	private int g;
	private int esgRating;
	private int contorversy;

	public EsgResult(Map<EsgObject, Double> positiveWeighted, Map<EsgObject, Double> negativeWeighted, int e, int s, int g, int esgRating, int contorversy) {
		this.positiveWeighted = positiveWeighted;
		this.negativeWeighted = negativeWeighted;
		this.e = e;
		this.s = s;
		this.g = g;
		this.esgRating = esgRating;
		this.contorversy = contorversy;
	}

	public int getE() {
		return e;
	}

	public void setE(int e) {
		this.e = e;
	}

	public int getS() {
		return s;
	}

	public void setS(int s) {
		this.s = s;
	}

	public int getG() {
		return g;
	}

	public void setG(int g) {
		this.g = g;
	}

	public int getEsgRating() {
		return esgRating;
	}

	public void setEsgRating(int esgRating) {
		this.esgRating = esgRating;
	}

	public int getContorversy() {
		return contorversy;
	}

	public void setContorversy(int contorversy) {
		this.contorversy = contorversy;
	}

	public Map<EsgObject, Double> getPositiveWeighted() {
		return positiveWeighted;
	}

	public Map<EsgObject, Double> getNegativeWeighted() {
		return negativeWeighted;
	}

	public void setPositiveWeighted(Map<EsgObject, Double> positiveWeighted) {
		this.positiveWeighted = positiveWeighted;
	}

	public void setNegativeWeighted(Map<EsgObject, Double> negativeWeighted) {
		this.negativeWeighted = negativeWeighted;
	}

}

//positiveWeighted.entrySet().stream().mapToDouble(entry -> entry.getValue()).sum();
