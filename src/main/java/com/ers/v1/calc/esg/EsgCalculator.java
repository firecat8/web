/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.calc.esg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.MfiTermsWrapperVo;

/**
 *
 * @author snyanakieva
 */
public class EsgCalculator {

	private final Map<EsgObject, Double> positiveWeighted = new HashMap<>();
	private final Map<EsgObject, Double> negativeWeighted = new HashMap<>();

	public EsgResult calculate(List<MfiTermsWrapperVo> terms, List<EsgObject> factors) {
		positiveWeighted.clear();
		negativeWeighted.clear();
		filterEsgFactors(terms, factors);

		double weightSumN = negativeWeighted.entrySet().stream()
				.mapToDouble(entry -> entry.getValue()).sum();
		double weightSumP = positiveWeighted.entrySet().stream()
				.mapToDouble(entry -> entry.getValue()).sum();
		double eSumP = positiveWeighted.entrySet().stream()
				.mapToDouble(entry -> entry.getValue() * entry.getKey().getE()).sum();
		double sSumP = positiveWeighted.entrySet().stream()
				.mapToDouble(entry -> entry.getValue() * entry.getKey().getS()).sum();
		double gSumP = positiveWeighted.entrySet().stream()
				.mapToDouble(entry -> entry.getValue() * entry.getKey().getG()).sum();
		double eSumN = negativeWeighted.entrySet().stream()
				.mapToDouble(entry -> entry.getValue() * entry.getKey().getE()).sum();
		double sSumN = negativeWeighted.entrySet().stream()
				.mapToDouble(entry -> entry.getValue() * entry.getKey().getS()).sum();
		double gSumN = negativeWeighted.entrySet().stream()
				.mapToDouble(entry -> entry.getValue() * entry.getKey().getG()).sum();

		int e = (int) (eSumP / weightSumP);
		int s = (int) (sSumP / weightSumP);
		int g = (int) (gSumP / weightSumP);
		int esgRating = (int) ((e + s + g) / 3);

		int en = (int) (eSumN / weightSumN);
		int sn = (int) (sSumN / weightSumN);
		int gn = (int) (gSumN / weightSumN);
		int controversy = (int) ((en + sn + gn) / 3);

		return new EsgResult(positiveWeighted, negativeWeighted, e, s, g, esgRating, controversy);
	}

	private void filterEsgFactors(List<MfiTermsWrapperVo> mfiAdapters, List<EsgObject> factors) {
		for (MfiTermsWrapperVo adapter : mfiAdapters) {
			EsgObject factor = factors.stream()
					.filter(x -> adapter.getKey().getFormulaTerm().getMarketFactorId().equals(x.getMfId()))
					.findFirst()
					.orElse(null);
			factor.setDescription(adapter.getValue());
			if (adapter.getKey().getFormulaTerm().getCoefficient() > 0) {
				positiveWeighted.put(factor, adapter.getKey().getFormulaTerm().getCoefficient());
			} else {
				negativeWeighted.put(factor, adapter.getKey().getFormulaTerm().getCoefficient());
			}
		}
	}
}
