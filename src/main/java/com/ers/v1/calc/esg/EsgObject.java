/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.calc.esg;

/**
 *
 * @author snyanakieva
 */
public class EsgObject {
	
	private String mfId;
	private String description;
	private int e;
	private int s;
	private int g;

	public EsgObject(String mfId, int e, int s, int g) {
		this.mfId = mfId;
		this.e = e;
		this.s = s;
		this.g = g;
	}
        
	public EsgObject(String mfId,String description, int e, int s, int g) {
		this.mfId = mfId;
                this.description = description;
		this.e = e;
		this.s = s;
		this.g = g;
	}
	
	public String getMfId() {
		return mfId;
	}

	public void setMfId(String mfId) {
		this.mfId = mfId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
	
	
}
