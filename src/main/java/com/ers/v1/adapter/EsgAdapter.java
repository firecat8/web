/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ers.v1.adapter;

import java.util.Arrays;

import com.ers.re.service.pub.api.v1_1.messaging.ResponseListener;
import com.ers.re.service.pub.api.v1_1.messaging.VoidListener;
import com.eurorisksystems.riskengine.ws.v1_1.vo.ErrorVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.FrequencyVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.ServiceStatusVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.TenorVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.math.SeriesTypeVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.math.statistics.SeriesParametersVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.portfolio.evaluation.EvaluationIdVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.SeriesSelectionCfgVo;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import com.ers.re.service.pub.api.v1_1.messaging.InOnlyChannel;
import com.ers.re.service.pub.api.v1_1.messaging.InOutChannel;
import static com.ers.v1.adapter.Adapter.LOGGER;
import com.ers.v1.servlet.esg.FactorsSelectionConfig;
import com.ers.v1.utils.MarketUtils;
import com.eurorisksystems.riskengine.ws.v1_1.vo.EntityIdVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.common.RegressionTypeVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.FindMfiFormulaRequestVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.GeneratedSeriesVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.InstrumentVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.LoadFactorsRequestVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.MfiTermsWrapperVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.SeriesAdapterCollectionVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.SuggestFactorsRequestVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.multifactor.FormulaSearchCfgVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.multifactor.FormulaTermVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.multifactor.MfiCalibrationStrategyVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.multifactor.MultiFactorInstrumentVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.multifactor.MultiFactorModelVo;

/**
 *
 * @author gdimitrova
 */
public class EsgAdapter extends CalculationAdapter {

	private MultiFactorInstrumentVo mfi;
	private GeneratedSeriesVo generatedSeries;
	private List<MfiTermsWrapperVo> series;

	public EvaluationIdVo createEvaluation() throws InterruptedException {
		final CountDownLatch createEvalLatch = new CountDownLatch(1);
		registry.getInstrumentSupportEndpoint().createEvaluation().invoke(createEvaluationResponseListener(createEvalLatch));
		createEvalLatch.await();
		return evalId;
	}

	public void loadFactors(FactorsSelectionConfig selectionConfig) throws InterruptedException {
		evalId = selectionConfig.getEvaluationIdVo();
		errors.clear();
		mfi = null;
		getMarketFactor(SeriesAdapter.MARKET_FACTORS.get(selectionConfig.getSeriesName() + "INEA"));

		if (!marketFactorVo.getInstrumentId().equals("dummy")) {
			loadMfi(marketFactorVo.getInstrumentId());
		}
		if (mfi == null) {
			errors.clear();
			mfi = makeAndSaveMultiFactorInstrumentVo(selectionConfig.getHistoricalInterval(), selectionConfig.getFrequency());
		} else {
			mfi.getModels().get(0).setHistoricalPeriod(selectionConfig.getHistoricalInterval());
			mfi.getModels().get(0).setFrequency(selectionConfig.getFrequency());
		}
		LOGGER.info("Start loading");
		final CountDownLatch loadFactorsLatch = new CountDownLatch(1);
		registry.getInstrumentSupportEndpoint().loadFactors().invoke(createLoadFactorsRequestVo(selectionConfig, mfi), new VoidListener() {
			@Override
			public void onSuccess() {
				LOGGER.info("Factors successfuly loaded");
				loadFactorsLatch.countDown();
			}

			@Override
			public void onError(ErrorVo error) {
				errors.add(error);
				LOGGER.info("Error loading factors");
				loadFactorsLatch.countDown();
			}

			@Override
			public void onNotify(ServiceStatusVo n) {
				LOGGER.log(Level.INFO, "\n Notify loading.\n Status:{0}\n State:{1}\n",
						new Object[]{n.getDescription(), n.getState().value()});
			}
		});
		loadFactorsLatch.await();

	}

	public void suggestFactors(FactorsSelectionConfig selectionConfig) throws InterruptedException {
		evalId = selectionConfig.getEvaluationIdVo();
		errors.clear();
		LOGGER.info("Start suggest");
		final CountDownLatch suggestFactorsLatch = new CountDownLatch(1);
		registry.getInstrumentSupportEndpoint().suggestFactors().invoke(createSuggestFactorsRequestVo(selectionConfig), new VoidListener() {
			@Override
			public void onSuccess() {
				LOGGER.info("factors successfuly suggested");
				suggestFactorsLatch.countDown();
			}

			@Override
			public void onError(ErrorVo error) {
				errors.add(error);
				suggestFactorsLatch.countDown();
			}

			@Override
			public void onNotify(ServiceStatusVo n) {
				LOGGER.log(Level.INFO, "\n Notify suggest.\n Status:{0}\n State:{1}\n",
						new Object[]{n.getDescription(), n.getState().value()});
			}
		});
		suggestFactorsLatch.await();

	}

	public void getFactors() throws InterruptedException {
		errors.clear();
		LOGGER.info("Start getting factors");
		final CountDownLatch getFactorsSeriesLatch = new CountDownLatch(1);
		registry.getInstrumentSupportEndpoint().getFactors().invoke(evalId, new ResponseListener<SeriesAdapterCollectionVo>() {
			@Override
			public void onSuccess(SeriesAdapterCollectionVo response) {
				LOGGER.info("Got factors");
				series = response.getSeries();
				getFactorsSeriesLatch.countDown();
			}

			@Override
			public void onError(ErrorVo error) {
				errors.add(error);
				getFactorsSeriesLatch.countDown();
			}

			@Override
			public void onNotify(ServiceStatusVo n) {
				LOGGER.log(Level.INFO, "\n Notify get factors.\n Status:{0}\n State:{1}\n",
						new Object[]{n.getDescription(), n.getState().value()});
			}
		});
		getFactorsSeriesLatch.await();
	}

	public void findFormula(List<FormulaTermVo> formulaTerms, String evalid) throws InterruptedException {
		getCalibratedMfi(evalid);
		mfi.getModels().get(0).setTerms(formulaTerms);
		saveInstrument(mfi);
		FindMfiFormulaRequestVo request = createFindMfiFormulaRequestVo();
		errors.clear();
		LOGGER.info("Start finding formula");
		final CountDownLatch findFormulaLatch = new CountDownLatch(1);
		registry.getInstrumentSupportEndpoint().findMfiFormula().invoke(request, new VoidListener() {
			@Override
			public void onSuccess() {
				LOGGER.info("Successfuly found formula");
				findFormulaLatch.countDown();
			}

			@Override
			public void onError(ErrorVo error) {
				errors.add(error);
				findFormulaLatch.countDown();
			}

			@Override
			public void onNotify(ServiceStatusVo n) {
				LOGGER.log(Level.INFO, "\n Notify find formula.\n Status:{0}\n State:{1}\n",
						new Object[]{n.getDescription(), n.getState().value()});
			}
		});
		findFormulaLatch.await();

	}

	public void getCalibratedMfi(String evalid) throws InterruptedException {
		evalId = new EvaluationIdVo(evalid);
		errors.clear();
		LOGGER.info("Start finding formula");
		final CountDownLatch getCalibratedMfiLatch = new CountDownLatch(1);
		registry.getInstrumentSupportEndpoint().getCalibratedMfi().invoke(evalId, new ResponseListener<MultiFactorInstrumentVo>() {
			@Override
			public void onSuccess(MultiFactorInstrumentVo response) {
				LOGGER.info("Successfuly found formula");
				mfi = response;
				getCalibratedMfiLatch.countDown();
			}

			@Override
			public void onError(ErrorVo error) {
				errors.add(error);
				getCalibratedMfiLatch.countDown();
			}

		});
		getCalibratedMfiLatch.await();

	}

	public void generateSeries() throws InterruptedException {
		errors.clear();
		LOGGER.info("Start generating series");
		final CountDownLatch generateSeriesLatch = new CountDownLatch(1);
		registry.getInstrumentSupportEndpoint().generateSeries().invoke(evalId, new VoidListener() {
			@Override
			public void onSuccess() {
				LOGGER.info("Successfuly generated series");
				generateSeriesLatch.countDown();
			}

			@Override
			public void onError(ErrorVo error) {
				errors.add(error);
				generateSeriesLatch.countDown();
			}

			@Override
			public void onNotify(ServiceStatusVo n) {
				LOGGER.log(Level.INFO, "\n Notify generating series.\n Status:{0}\n State:{1}\n",
						new Object[]{n.getDescription(), n.getState().value()});
			}
		});
		generateSeriesLatch.await();

	}

	public void loadGeneratedSeries() throws InterruptedException {
		errors.clear();
		LOGGER.info("Start getting series");
		final CountDownLatch loadGeneratedSeriesLatch = new CountDownLatch(1);
		registry.getInstrumentSupportEndpoint().getGeneratedSeries().invoke(evalId, new ResponseListener<GeneratedSeriesVo>() {
			@Override
			public void onSuccess(GeneratedSeriesVo response) {
				LOGGER.info("Series got");
				generatedSeries = response;
				loadGeneratedSeriesLatch.countDown();
			}

			@Override
			public void onError(ErrorVo error) {
				errors.add(error);
				loadGeneratedSeriesLatch.countDown();
			}

		});
		loadGeneratedSeriesLatch.await();

	}

	public List<MfiTermsWrapperVo> getSeries() {
		return series;
	}

	public MultiFactorInstrumentVo getMfi() {
		return mfi;
	}

	public GeneratedSeriesVo getGeneratedSeries() {
		return generatedSeries;
	}

	private void loadMfi(String mfiId) throws InterruptedException {
		LOGGER.log(Level.INFO, "Loading multifactor instrument with id: {0} ", mfiId);
		final CountDownLatch loadMfiLatch = new CountDownLatch(1);
		registry.getInstrumentEndpoint().loadById().invoke(new EntityIdVo(new GregorianCalendar().getTimeInMillis(), mfiId), new ResponseListener<InstrumentVo>() {
			@Override
			public void onSuccess(InstrumentVo response) {
				LOGGER.info("Got multifactor instrument");
				if (response instanceof MultiFactorInstrumentVo) {
					mfi = (MultiFactorInstrumentVo) response;
				}
				loadMfiLatch.countDown();
			}

			@Override
			public void onError(ErrorVo error) {
				errors.add(error);
				loadMfiLatch.countDown();
			}

			@Override
			public void onNotify(ServiceStatusVo n) {
				LOGGER.log(Level.INFO, "\n Notify get instrument.\n Status:{0}\n State:{1}\n",
						new Object[]{n.getDescription(), n.getState().value()});
			}
		});
		loadMfiLatch.await();
	}

	private SuggestFactorsRequestVo createSuggestFactorsRequestVo(FactorsSelectionConfig selectionConfig) {
		SuggestFactorsRequestVo suggestFactorsRequestVo = new SuggestFactorsRequestVo();
		suggestFactorsRequestVo.setEvaluationId(evalId);
		suggestFactorsRequestVo.setSeriesSelectionCfg(createSeriesSelectionCfgVo(selectionConfig));
		return suggestFactorsRequestVo;
	}

	private LoadFactorsRequestVo createLoadFactorsRequestVo(FactorsSelectionConfig selectionConfig, MultiFactorInstrumentVo mfi) {
		LoadFactorsRequestVo loadFactorsRequestVo = new LoadFactorsRequestVo();
		loadFactorsRequestVo.setEvalCtx(MarketUtils.INSTANCE.getOrMakeEvalCtx());
		loadFactorsRequestVo.setEvaluationId(evalId);
		loadFactorsRequestVo.setMultifactorInstrument(mfi);
		loadFactorsRequestVo.setSeriesSelectionCfg(createSeriesSelectionCfgVo(selectionConfig));
		return loadFactorsRequestVo;
	}

	private SeriesSelectionCfgVo createSeriesSelectionCfgVo(FactorsSelectionConfig selectionConfig) {
		SeriesSelectionCfgVo selectionCfgVo = new SeriesSelectionCfgVo();
		selectionCfgVo.setDecayFactor(1.0);
		selectionCfgVo.setFactorSelector(selectionConfig.getSuggestionMethod());
		selectionCfgVo.setFilterByESG(Boolean.TRUE);
		selectionCfgVo.setMaxNumberOfSeries(selectionConfig.getMaxSuggestions());
		selectionCfgVo.setUseAbsoluteCorrelations(Boolean.FALSE);
		selectionCfgVo.setMinSeriesQuality(selectionConfig.getMinimalQuality());
		selectionCfgVo.setSeriesParameters(
				new SeriesParametersVo(
						selectionConfig.getFrequency(),
						SeriesTypeVo.ABSOLUTE,
						selectionConfig.getHistoricalInterval(),
						new GregorianCalendar(),
						Boolean.FALSE));
		return selectionCfgVo;
	}

	private MultiFactorInstrumentVo makeAndSaveMultiFactorInstrumentVo(TenorVo historicalInterval, FrequencyVo frequency) throws InterruptedException {
		MultiFactorInstrumentVo mfiVo = new MultiFactorInstrumentVo();
		mfiVo.setActiveModelName("activeModel");
		mfiVo.setCurrency(MarketUtils.INSTANCE.getDafaultCurrency());
		mfiVo.setId(UUID.randomUUID().toString());
		mfiVo.setMarketId(MarketUtils.INSTANCE.getMarketId());
		mfiVo.setName(UUID.randomUUID().toString());
		mfiVo.setOtc(Boolean.FALSE);
		mfiVo.setQuotedUnits(100.0);
		mfiVo.setValidFrom(MarketUtils.INSTANCE.getValidFrom());
		MultiFactorModelVo modelVo = new MultiFactorModelVo();
		modelVo.setCalibrationStrategy(MfiCalibrationStrategyVo.MANUAL);
		modelVo.setDecayFactor(1.0);
		modelVo.setFormulaSearchConfiguration(
				new FormulaSearchCfgVo(
						Boolean.FALSE,
						Boolean.FALSE,
						Boolean.FALSE,
						Boolean.FALSE,
						Boolean.FALSE,
						Boolean.FALSE,
						Boolean.FALSE,
						Boolean.FALSE,
						Boolean.FALSE,
						Boolean.FALSE,
						Boolean.FALSE,
						Boolean.FALSE,
						Boolean.TRUE,
						1.0,
						1.0,
						1.0,
						Boolean.FALSE,
						0,
						10,
						Boolean.FALSE
				));
		modelVo.setFrequency(frequency);
		modelVo.setHistoricalPeriod(historicalInterval);
		modelVo.setName("activeModel");
		modelVo.setRegressionType(RegressionTypeVo.ORDINARY);
		modelVo.setSeriesType(SeriesTypeVo.ABSOLUTE);
		mfiVo.getModels().clear();
		mfiVo.getModels().add(modelVo);
		//set as market factor underlying
		saveMarketFactor(mfiVo.getId());
		return mfiVo;
	}

	private FindMfiFormulaRequestVo createFindMfiFormulaRequestVo() {
		FindMfiFormulaRequestVo requestVo = new FindMfiFormulaRequestVo();
		requestVo.setCalibratedModels(Arrays.asList("activeModel"));
		requestVo.setEvaluationId(evalId);
		requestVo.setInstrument(mfi);
		requestVo.setMarketDate(MarketUtils.INSTANCE.getOrMakeEvalCtx().getEvaluationDate());
		requestVo.setMarketId(MarketUtils.INSTANCE.getMarketId());
		requestVo.setProviderId(MarketUtils.INSTANCE.getProviderId());
		requestVo.setSeriesNormalizatorConfig(MarketUtils.INSTANCE.getOrMakeEvalCtx().getSeriesNormalizatorConfig());
		return requestVo;
	}

	@Override
	protected InOnlyChannel<EvaluationIdVo> delete() {
		return registry.getInstrumentSupportEndpoint().delete();
	}

	@Override
	protected InOutChannel<EvaluationIdVo, ServiceStatusVo> getStatus() {
		return registry.getInstrumentSupportEndpoint().getStatus();
	}

}
