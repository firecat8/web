/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.adapter;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import com.ers.re.service.pub.api.v1_1.messaging.InOnlyChannel;
import com.ers.re.service.pub.api.v1_1.messaging.InOutChannel;
import com.ers.re.service.pub.api.v1_1.messaging.ResponseListener;
import com.ers.re.service.pub.api.v1_1.messaging.VoidListener;
import static com.ers.v1.adapter.Adapter.LOGGER;
import com.eurorisksystems.riskengine.ws.v1_1.vo.EntityIdVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.ErrorVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.ServiceStatusVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.StateVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.instrument.InstrumentVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.market.factor.AbstractMarketFactorVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.market.factor.InstrumentMarketFactorVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.portfolio.evaluation.EvaluationIdVo;

/**
 *
 * @author snyanakieva
 */
public abstract class CalculationAdapter extends Adapter {

    protected EvaluationIdVo evalId = null;
    protected ServiceStatusVo status = new ServiceStatusVo();
    protected InstrumentMarketFactorVo marketFactorVo = null;

    public InstrumentMarketFactorVo getMarketFactorVo() {
        return marketFactorVo;
    }

    public StateVo getState() {
        return status.getState();
    }

    public void setEvaluationId(EvaluationIdVo evalId) {
        this.evalId = evalId;
    }

    public void deleteEval() throws InterruptedException {
        LOGGER.info("Delete evaluation");
        final CountDownLatch deleteLatch = new CountDownLatch(1);
        delete().invoke(evalId, new VoidListener() {
            @Override
            public void onSuccess() {
                LOGGER.info("Delete evaluation is done");
                deleteLatch.countDown();
            }

            @Override
            public void onError(ErrorVo error) {
                errors.add(error);
                deleteLatch.countDown();
            }
        });
        deleteLatch.await();
        if (!errors.isEmpty()) {
            errors.forEach((error) -> {
                LOGGER.severe(error.getErrors().toString());
            });
        }
    }

    public ServiceStatusVo checkStatus() throws InterruptedException {
        final CountDownLatch getStatusLatch = new CountDownLatch(1);
        getStatus().invoke(evalId, new ResponseListener<ServiceStatusVo>() {
            @Override
            public void onSuccess(ServiceStatusVo rspns) {
                status = rspns;
                getStatusLatch.countDown();
            }

            @Override
            public void onError(ErrorVo ev) {
                status = null;
                getStatusLatch.countDown();
            }
        });
        getStatusLatch.await();
        if (!errors.isEmpty()) {
            errors.forEach((error) -> {
                LOGGER.severe(error.getErrors().toString());
            });
        }
        return status;
    }

    public ResponseListener<EvaluationIdVo> createEvaluationResponseListener(CountDownLatch createEvalLatch) throws InterruptedException {
        evalId = null;
        LOGGER.info("Start evaluation creation");
        return new ResponseListener<EvaluationIdVo>() {
            @Override
            public void onSuccess(EvaluationIdVo response) {
                LOGGER.info("Success evaluation creation");
                evalId = response;
                createEvalLatch.countDown();
            }

            @Override
            public void onError(ErrorVo error) {
                errors.add(error);
                createEvalLatch.countDown();
            }

            @Override
            public void onNotify(ServiceStatusVo n) {
                LOGGER.log(Level.INFO, "\n Notify evaluation creation.\n Status:{0}\n State:{1}\n",
                        new Object[]{n.getDescription(), n.getState().value()});
            }
        };
    }

    public void getMarketFactor(String mfId) throws InterruptedException {
        EntityIdVo entityIdVo = new EntityIdVo();
        entityIdVo.setId(mfId);
        final CountDownLatch getMarketFactorLatch = new CountDownLatch(1);
        registry.getMarketFactorEndpoint().loadById().invoke(entityIdVo, new ResponseListener<AbstractMarketFactorVo>() {
            @Override
            public void onSuccess(AbstractMarketFactorVo rspns) {
                marketFactorVo = (InstrumentMarketFactorVo) rspns;
                getMarketFactorLatch.countDown();
            }

            @Override
            public void onError(ErrorVo ev) {
                errors.add(ev);
                getMarketFactorLatch.countDown();
            }

            @Override
            public void onNotify(ServiceStatusVo n) {
                LOGGER.log(Level.INFO, "\n Notify \n getMarketFactor .\n Status:{0}\n State:{1}\n",
                        new Object[]{n.getDescription(), n.getState().value()});
            }
        });

        getMarketFactorLatch.await();
    }

    protected void saveInstrument(InstrumentVo instrument) throws InterruptedException {
        LOGGER.info("Start Saving of instrument");
        final CountDownLatch saveLatch = new CountDownLatch(1);
        registry.getInstrumentEndpoint().saveOrUpdate().invoke(instrument, new VoidListener() {
            @Override
            public void onSuccess() {
                LOGGER.info("Saved instrument");
                saveLatch.countDown();
            }

            @Override
            public void onError(ErrorVo error) {
                errors.add(error);
                saveLatch.countDown();
            }

            @Override
            public void onNotify(ServiceStatusVo n) {
                LOGGER.log(Level.INFO, "\n Notify saving instrument.\n Status:{0}\n State:{1}\n",
                        new Object[]{n.getDescription(), n.getState().value()});
            }
        });
        saveLatch.await();
    }

    public void deleteInstrument(String instrumentId) throws InterruptedException {
        errors.clear();
        LOGGER.info("Start deleting of instrument");
        final CountDownLatch deleteLatch = new CountDownLatch(1);
        registry.getInstrumentEndpoint().delete().invoke(new EntityIdVo(null, instrumentId),
                new VoidListener() {
            @Override
            public void onSuccess() {
                LOGGER.info("Deleted instrument");
                deleteLatch.countDown();
            }

            @Override
            public void onError(ErrorVo error) {
                errors.add(error);
                deleteLatch.countDown();
            }
        });
        deleteLatch.await();
    }

    protected void saveMarketFactor(String id) throws InterruptedException {
        final CountDownLatch saveLatch = new CountDownLatch(1);
        marketFactorVo.setInstrumentId(id);
        registry.getMarketFactorEndpoint().saveOrUpdate().invoke(marketFactorVo, new VoidListener() {
            @Override
            public void onSuccess() {
                LOGGER.info("Saved instrument factor");
                saveLatch.countDown();
            }

            @Override
            public void onError(ErrorVo error) {
                errors.add(error);
                saveLatch.countDown();
            }

            @Override
            public void onNotify(ServiceStatusVo n) {
                status = n;
                LOGGER.log(Level.INFO, "\n Notify instrument factor.\n Status:{0}\n State:{1}\n",
                        new Object[]{n.getDescription(), n.getState().value()});
            }
        });
        saveLatch.await();
    }

    protected abstract InOnlyChannel<EvaluationIdVo> delete();

    protected abstract InOutChannel<EvaluationIdVo, ServiceStatusVo> getStatus();
}
