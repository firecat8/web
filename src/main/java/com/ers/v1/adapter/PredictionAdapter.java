/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.adapter;

import com.ers.re.service.pub.api.v1_1.messaging.ResponseListener;
import com.ers.re.service.pub.api.v1_1.messaging.VoidListener;
import com.ers.v1.utils.MarketUtils;
import com.eurorisksystems.riskengine.ws.v1_1.vo.ErrorVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.ServiceStatusVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.market.factor.InstrumentMarketFactorVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.portfolio.evaluation.EvaluationIdVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.prediction.PredictionConfigVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.prediction.PredictionRequestVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.prediction.PredictionResultVo;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import com.ers.re.service.pub.api.v1_1.messaging.InOnlyChannel;
import com.ers.re.service.pub.api.v1_1.messaging.InOutChannel;

/**
 *
 * @author gdimitrova
 */
public class PredictionAdapter extends CalculationAdapter {

    private final String ID = "CommodityINEA_" + UUID.randomUUID().toString();
    private PredictionResultVo results;

    public PredictionResultVo getResults() {
        return results;
    }

    public String getID() {
        return ID;
    }

    public void createInstrument(String mfId) throws InterruptedException {
        errors.clear();
        saveCommodity();
        getMarketFactor(mfId);
        saveMarketFactor(ID);
    }

    public EvaluationIdVo createEvaluation(PredictionConfigVo predictionConfig) throws InterruptedException {
        PredictionRequestVo makePredictionRequest = makePredictionRequest(predictionConfig, marketFactorVo.getInstrumentId());
        final CountDownLatch createEvalLatch = new CountDownLatch(1);
        registry.getFundPricePredictionEndpoint().createEvaluation().invoke(makePredictionRequest, createEvaluationResponseListener(createEvalLatch));
        createEvalLatch.await();
        return evalId;
    }

    public void predictFundPrice() throws InterruptedException {
        LOGGER.info("Start prediction");
        final CountDownLatch predictFundPriceLatch = new CountDownLatch(1);
        registry.getFundPricePredictionEndpoint().predictFundPrice().invoke(evalId, new VoidListener() {
            @Override
            public void onSuccess() {
                LOGGER.info("Success prediction");
                predictFundPriceLatch.countDown();
            }

            @Override
            public void onError(ErrorVo error) {
                errors.add(error);
                predictFundPriceLatch.countDown();
            }

            @Override
            public void onNotify(ServiceStatusVo n) {
                LOGGER.log(Level.INFO, "\n Notify Prediction.\n Status:{0}\n State:{1}\n",
                        new Object[]{n.getDescription(), n.getState().value()});
            }
        });
        predictFundPriceLatch.await();

    }

    public void getPredictionResults() throws InterruptedException {
        LOGGER.info("Start getting prediction results");
        final CountDownLatch getPredictionResultsLatch = new CountDownLatch(1);
        registry.getFundPricePredictionEndpoint().getPredictionResults().invoke(evalId, new ResponseListener<PredictionResultVo>() {
            @Override
            public void onSuccess(PredictionResultVo response) {
                LOGGER.info("Got prediction results");
                results = response;
                getPredictionResultsLatch.countDown();
            }

            @Override
            public void onError(ErrorVo error) {
                errors.add(error);
                getPredictionResultsLatch.countDown();
            }

            @Override
            public void onNotify(ServiceStatusVo n) {
                LOGGER.log(Level.INFO, "\n Notify getPredictionResults.\n Status:{0}\n State:{1}\n",
                        new Object[]{n.getDescription(), n.getState().value()});
            }
        });
        getPredictionResultsLatch.await();
    }

    @Override
    protected InOnlyChannel<EvaluationIdVo> delete() {
        return registry.getFundPricePredictionEndpoint().delete();
    }

    @Override
    protected InOutChannel<EvaluationIdVo, ServiceStatusVo> getStatus() {
        return registry.getFundPricePredictionEndpoint().getStatus();
    }

    private PredictionRequestVo makePredictionRequest(PredictionConfigVo predictionConfig, String instrumentId) {
        PredictionRequestVo predictionRequestVo = new PredictionRequestVo();
        predictionRequestVo.setEvalCtx(MarketUtils.INSTANCE.getOrMakeEvalCtx());
        predictionRequestVo.setInstrumentId(instrumentId);
        predictionRequestVo.setPredictionCfg(predictionConfig);
        return predictionRequestVo;
    }

    private void saveCommodity() throws InterruptedException {
        saveInstrument(MarketUtils.INSTANCE.makeCommodity(ID));
    }
}
