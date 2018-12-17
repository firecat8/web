/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.servlet.esg;

import com.eurorisksystems.riskengine.ws.v1_1.vo.FrequencyVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.TenorVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.FactorSelectorVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.portfolio.evaluation.EvaluationIdVo;

/**
 *
 * @author snyanakieva
 */
public class FactorsSelectionConfig {

	private String seriesName;
	private TenorVo historicalInterval;
	private FrequencyVo frequency;
	private FactorSelectorVo suggestionMethod;
	private int maxSuggestions;
	private double minimalQuality;
	private EvaluationIdVo evaluationIdVo;

	public FactorsSelectionConfig() {
	}

	public FactorsSelectionConfig(String seriesName, TenorVo historicalInterval, FrequencyVo frequency, FactorSelectorVo suggestionMethod, int maxSuggestions, double minimalQuality, EvaluationIdVo evaluationIdVo) {
		this.seriesName = seriesName;
		this.historicalInterval = historicalInterval;
		this.frequency = frequency;
		this.suggestionMethod = suggestionMethod;
		this.maxSuggestions = maxSuggestions;
		this.minimalQuality = minimalQuality;
		this.evaluationIdVo = evaluationIdVo;
	}


	public String getSeriesName() {
		return seriesName;
	}

	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}

	public TenorVo getHistoricalInterval() {
		return historicalInterval;
	}

	public void setHistoricalInterval(TenorVo historicalInterval) {
		this.historicalInterval = historicalInterval;
	}

	public FrequencyVo getFrequency() {
		return frequency;
	}

	public void setFrequency(FrequencyVo frequency) {
		this.frequency = frequency;
	}

	public FactorSelectorVo getSuggestionMethod() {
		return suggestionMethod;
	}

	public void setSuggestionMethod(FactorSelectorVo suggestionMethod) {
		this.suggestionMethod = suggestionMethod;
	}

	public int getMaxSuggestions() {
		return maxSuggestions;
	}

	public void setMaxSuggestions(int maxSuggestions) {
		this.maxSuggestions = maxSuggestions;
	}

	public double getMinimalQuality() {
		return minimalQuality;
	}

	public void setMinimalQuality(double minimalQuality) {
		this.minimalQuality = minimalQuality;
	}

	public EvaluationIdVo getEvaluationIdVo() {
		return evaluationIdVo;
	}

	public void setEvaluationIdVo(EvaluationIdVo evaluationIdVo) {
		this.evaluationIdVo = evaluationIdVo;
	}
}
