/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.utils;

import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;

import com.eurorisksystems.riskengine.ws.v1_1.vo.CurrencyVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.FrequencyVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.PaidPriceAggregationVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.SiLinkModelVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.TenorVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.VarSettingsVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.WeightedAggregationTypeVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.CommodityVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.InstrumentVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.market.series.LinearInterpolationNormalizatorConfigVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.math.SeriesTypeVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.math.statistics.SeriesParametersVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.portfolio.evaluation.EvaluationContextVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.portfolio.evaluation.NoCalibrationVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.portfolio.evaluation.RemainingDebtVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.portfolio.evaluation.YieldCurveInterpolationTypeVo;

/**
 *
 * @author snyanakieva
 */
public class MarketUtils {

    public static MarketUtils INSTANCE = new MarketUtils();

    private final long commodityDate = new GregorianCalendar(1995, 0, 1).getTimeInMillis();

    private EvaluationContextVo evalCtx;

    public EvaluationContextVo getOrMakeEvalCtx() {
        if (evalCtx != null) {
            return evalCtx;
        } else {
            Calendar evalCalendar = Calendar.getInstance();
            evalCtx = new EvaluationContextVo();
            evalCtx.setLoadDate(evalCalendar);
            evalCtx.setEvaluationDate(evalCalendar);
            evalCtx.setMarketDate(evalCalendar);
            evalCtx.setScenarioId("RE_PFD_MarketScenario_01");
            evalCtx.setTransactionAggregation(PaidPriceAggregationVo.WEIGHTED_AVERAGE);
            evalCtx.setCurrency(getDafaultCurrency());
            evalCtx.setSeriesParameters(new SeriesParametersVo(FrequencyVo.DAILY, SeriesTypeVo.ABSOLUTE, new TenorVo(1, 0, 0), evalCalendar, Boolean.FALSE));
            evalCtx.setWithMarketScenarios(true);
            evalCtx.setAssetVsLiabilityAggregation(WeightedAggregationTypeVo.CLOSED_CAPITAL);
            evalCtx.setVarSettings(new VarSettingsVo(0.95, 4000, 100, 1, false, true, 0L));
            evalCtx.setOtcMarketValueStrategy(new RemainingDebtVo(true));
            evalCtx.setModelToMarketCalibration(new NoCalibrationVo());
            evalCtx.setSimulateFxForwardInterest(true);
            evalCtx.setSmithWilsonConfiguration(Collections.EMPTY_LIST);
            evalCtx.setYcInterpolationType(YieldCurveInterpolationTypeVo.LINEAR);
            evalCtx.setCrossFxReferenceCurrency(getDafaultCurrency());
            evalCtx.setSeriesNormalizatorConfig(new LinearInterpolationNormalizatorConfigVo("RE_PFD_Calendar_01"));

            return evalCtx;
        }
    }

    public String getMarketId() {
        return "RE_PFD_Market";
    }

    public String getProviderId() {
        return "RE_PFD_Provider";
    }

    public long getValidFrom() {
        return new GregorianCalendar(1995, 1, 1).getTimeInMillis();
    }

    public CurrencyVo getDafaultCurrency() {
        return CurrencyVo.USD;
    }

    public InstrumentVo makeCommodity(final String ID) {
        CommodityVo vo = new CommodityVo();
        vo.setId(ID);
        vo.setValidFrom(commodityDate);
        vo.setMarketId(MarketUtils.INSTANCE.getMarketId());
        vo.setName(ID);
        vo.setDescription("CommodityVo description");
        vo.setCurrency(CurrencyVo.EUR);
        vo.setStockIndexId(null);
        vo.setSiLinkModel(SiLinkModelVo.BASE_FACTOR);
        vo.setOtc(false);
        vo.setQuotedUnits(0.2);
        vo.setAutomaticWindowSize(Boolean.TRUE);
        vo.setHistoricalPriceFrequency(FrequencyVo.MONTHLY);
        vo.setHistoricalInterval(new TenorVo(4, 0, 0));
        return vo;
    }
    
}
