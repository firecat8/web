/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.adapter;

import com.ers.re.service.pub.api.v1_1.messaging.VoidListener;
import com.eurorisksystems.riskengine.ws.v1_1.vo.ErrorVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.ServiceStatusVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.market.factor.InstrumentMarketFactorVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.market.factor.InstrumentValueVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.market.factor.quote.MarketFactorQuoteCollectionVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.market.factor.quote.MarketFactorQuoteIdVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.market.factor.quote.MarketFactorQuoteVo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ers.re.service.pub.api.v1_1.messaging.ResponseListener;
import static com.ers.v1.adapter.Adapter.LOGGER;
import com.ers.v1.utils.MarketUtils;
import com.eurorisksystems.riskengine.ws.v1_1.vo.EntityIdVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.IntegerVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.LongVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.StringPatternConfigVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.descriptions.IdNameDescriptionsVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.descriptions.MarketFactorQuoteDescriptionVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.descriptions.MarketFactorQuoteDescriptionsVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.filter.FilterTypeVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.filter.MarketFactorFilterVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.filter.MarketFactorQuoteFilterVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.filter.SnapshotFilterTypeVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.market.factor.quote.HistorizedMarketFactorQuoteIdVo;

/**
 *
 * @author snyanakieva
 */
public class SeriesAdapter extends Adapter {

    public static Map<String, String> MARKET_FACTORS = new HashMap<String, String>();
    private int batchSize = 400;//default
    private long quotesCount = 0;

    private final Comparator AT_TIME_COMPARATOR = new Comparator<MarketFactorQuoteDescriptionVo>() {
        @Override
        public int compare(MarketFactorQuoteDescriptionVo o1, MarketFactorQuoteDescriptionVo o2) {
            long c = o1.getEntityId().getAtTime() - o2.getEntityId().getAtTime();
            return (int) c;
        }
    };

    private MarketFactorQuoteDescriptionsVo marketFactorQuoteDescriptions;

    public void loadIneaFactors() {
        errors.clear();
        try {
            final CountDownLatch loadIneaFactorsLatch = new CountDownLatch(1);
            registry.getMarketFactorEndpoint()
                    .search().invoke(new MarketFactorFilterVo(0, 400,
                            new SnapshotFilterTypeVo(new GregorianCalendar(1900, 1, 1).getTimeInMillis(),
                                    new GregorianCalendar().getTimeInMillis()),
                            true,
                            "INEA",
                            StringPatternConfigVo.ANY_FIELD_PART,
                            null,
                            null,
                            null,
                            Collections.EMPTY_LIST,
                            Collections.EMPTY_LIST,
                            Collections.EMPTY_LIST,
                            Collections.EMPTY_LIST,
                            Collections.EMPTY_LIST,
                            Collections.EMPTY_LIST,
                            Collections.EMPTY_LIST,
                            Collections.EMPTY_LIST,
                            Collections.EMPTY_LIST,
                            Collections.EMPTY_LIST
                    ),
                            new ResponseListener<IdNameDescriptionsVo>() {
                        @Override
                        public void onSuccess(IdNameDescriptionsVo rspns) {
                            rspns.getIdNameDescriptions().forEach(description
                                    -> SeriesAdapter.MARKET_FACTORS.put(
                                            description.getName(),
                                            description.getEntityId().getId())
                            );
                            loadIneaFactorsLatch.countDown();
                        }

                        @Override
                        public void onError(ErrorVo ev) {
                            errors.add(ev);
                            loadIneaFactorsLatch.countDown();
                        }

                    });
            loadIneaFactorsLatch.await();

        } catch (InterruptedException ex) {
            Logger.getLogger(SeriesAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveSeries(String marketFactorName, Map<Calendar, Double> series) throws InterruptedException {
        errors.clear();
        InstrumentMarketFactorVo mf = makeInstrumentMarketFactorVo(marketFactorName);
        saveMarketFactor(mf);
        getBatchSize();
        saveQuotes(series, mf.getId());
        if (!errors.isEmpty()) {
            errors.forEach((error) -> {
                LOGGER.severe(error.getErrors().toString());
            });
        }
    }

    public void deleteSeries(InstrumentMarketFactorVo marketFactorVo, Map<Calendar, Double> quotes) throws InterruptedException {
        errors.clear();
        deleteQuotes(marketFactorVo.getId(), quotes);
        deleteMarketFactor(marketFactorVo);
        if (!errors.isEmpty()) {
            errors.forEach((error) -> {
                LOGGER.severe(error.getErrors().toString());
            });
        }
    }

    public List<MarketFactorQuoteDescriptionVo> loadSeries(String marketFactorId, Calendar startDate) {
        errors.clear();
        try {
            final CountDownLatch loadSeriesLatch = new CountDownLatch(1);
            registry.getMarketFactorQuoteEndpoint()
                    .search().invoke(new MarketFactorQuoteFilterVo(0, 5000,
                            new SnapshotFilterTypeVo(startDate.getTimeInMillis(),
                                    new GregorianCalendar().getTimeInMillis()),
                            MarketUtils.INSTANCE.getMarketId(),
                            MarketUtils.INSTANCE.getProviderId(),
                            marketFactorId
                    ),
                            new ResponseListener<MarketFactorQuoteDescriptionsVo>() {
                        @Override
                        public void onSuccess(MarketFactorQuoteDescriptionsVo rspns) {
                            marketFactorQuoteDescriptions = rspns;
                            loadSeriesLatch.countDown();
                        }

                        @Override
                        public void onError(ErrorVo ev) {
                            errors.add(ev);
                            loadSeriesLatch.countDown();
                        }

                    });
            loadSeriesLatch.await();

            List<MarketFactorQuoteDescriptionVo> descriptions = marketFactorQuoteDescriptions.getMarketFactorQuotesDescriptions();
            Collections.sort(descriptions, AT_TIME_COMPARATOR);
            return descriptions;

        } catch (InterruptedException ex) {
            Logger.getLogger(SeriesAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public List<MarketFactorQuoteDescriptionVo> loadSeries(String marketFactorId) {
        return loadSeries(marketFactorId, new GregorianCalendar(2010, 1, 1));
    }

    private void getBatchSize() throws InterruptedException {
        final CountDownLatch getBatchSizeLatch = new CountDownLatch(1);
        registry.getBatchEndpoint().getBatchSize().invoke(new ResponseListener<IntegerVo>() {
            @Override
            public void onSuccess(IntegerVo rspns) {
                batchSize = rspns.getResponse();
                getBatchSizeLatch.countDown();
            }

            @Override
            public void onError(ErrorVo ev) {
                errors.add(ev);
                getBatchSizeLatch.countDown();
            }
        });
        getBatchSizeLatch.await();
    }

    private void saveMarketFactor(InstrumentMarketFactorVo mf) throws InterruptedException {
        final CountDownLatch saveLatch = new CountDownLatch(1);

        SeriesAdapter.MARKET_FACTORS.put(mf.getName(), mf.getId());
        registry.getMarketFactorEndpoint().saveOrUpdate().invoke(mf, new VoidListener() {
            @Override
            public void onSuccess() {
                LOGGER.info("Saved instrument market factor");
                saveLatch.countDown();
            }

            @Override
            public void onError(ErrorVo error) {
                errors.add(error);
                saveLatch.countDown();
            }

            @Override
            public void onNotify(ServiceStatusVo n) {
                LOGGER.log(Level.INFO, "\n Notify instrument factor.\n Status:{0}\n State:{1}\n",
                        new Object[]{n.getDescription(), n.getState().value()});
            }
        });
        saveLatch.await();
    }
    private void saveQuotes(Map<Calendar, Double> series, String mfId) throws InterruptedException {
        List<MarketFactorQuoteVo> mfQuotesVo = makeMfQuotes(series, mfId);
        int size = mfQuotesVo.size();
        int counter = (size % batchSize) == 0 ? size / batchSize : size / batchSize + 1;
        final CountDownLatch saveQuotesLatch = new CountDownLatch(counter);
        int next = batchSize - 1 < size ? batchSize - 1 : size - 1;
        for (int i = 0; next < size && i < size;) {
            registry.getMarketFactorQuoteEndpoint()
                    .saveOrUpdateAll().invoke(new MarketFactorQuoteCollectionVo(null, mfQuotesVo.subList(i, next + 1)),
                            new VoidListener() {
                        @Override
                        public void onSuccess() {
                            LOGGER.info("Saved quotes");
                            saveQuotesLatch.countDown();
                        }

                        @Override
                        public void onError(ErrorVo ev) {
                            errors.add(ev);
                            saveQuotesLatch.countDown();
                        }

                        @Override
                        public void onNotify(ServiceStatusVo n) {
                            LOGGER.log(Level.INFO, "\n Notify saving quotes.\n Status:{0}\n State:{1}\n",
                                    new Object[]{n.getDescription(), n.getState().value()});
                        }
                    });
            next = (next + batchSize) < size ? next + batchSize : size - 1;
            i += batchSize;
        }
        saveQuotesLatch.await();
    }

    private void deleteMarketFactor(InstrumentMarketFactorVo marketFactorVo) throws InterruptedException {
        LOGGER.info("Start deleting market factor ");
        final CountDownLatch deleteMarketFactorLatch = new CountDownLatch(1);
        registry.getMarketFactorEndpoint().deleteSnapshot().invoke(new EntityIdVo(marketFactorVo.getValidFrom(), marketFactorVo.getId()), new VoidListener() {
            @Override
            public void onSuccess() {
                LOGGER.info("Deleted market factor ".concat(marketFactorVo.getName()));
                deleteMarketFactorLatch.countDown();
            }

            @Override
            public void onError(ErrorVo error) {
                errors.add(error);
                deleteMarketFactorLatch.countDown();
            }
        });
        deleteMarketFactorLatch.await();
    }

    private InstrumentMarketFactorVo makeInstrumentMarketFactorVo(String name) {
        return new InstrumentMarketFactorVo(
                MarketUtils.INSTANCE.getValidFrom(),
                null,
                name + "_" + UUID.randomUUID().toString(),
                name + "INEA",
                false,
                false,
                false,
                null,
                "dummy",
                InstrumentValueVo.MID
        );
    }

    private List<MarketFactorQuoteVo> makeMfQuotes(Map<Calendar, Double> series, String mfId) {
        List<MarketFactorQuoteVo> mfQuotes = new ArrayList<>();
        MarketFactorQuoteIdVo mfQuoteId = makeMarketFactorQuoteIdVo(mfId);
        series.entrySet().forEach((serie) -> {
            mfQuotes.add(makeMarketFactorQuoteVo(serie.getKey(), mfQuoteId, serie.getValue()));
        });
        return mfQuotes;
    }

    private MarketFactorQuoteIdVo makeMarketFactorQuoteIdVo(String mfId) {
        return new MarketFactorQuoteIdVo(mfId, MarketUtils.INSTANCE.getMarketId(), MarketUtils.INSTANCE.getProviderId());
    }

    private MarketFactorQuoteVo makeMarketFactorQuoteVo(Calendar date, MarketFactorQuoteIdVo mfQuoteId, double value) {
        return new MarketFactorQuoteVo(date.getTimeInMillis(), null, mfQuoteId, value, "mfquote", 1.);
    }

    private void deleteQuotes(String mfId, Map<Calendar, Double> quotes) throws InterruptedException {
        LOGGER.log(Level.INFO, "Start deleting quotes {0}", quotes.size());
        final CountDownLatch deleteQuotesLatch = new CountDownLatch(quotes.size());
        MarketFactorQuoteIdVo mfQuoteIdVo = makeMarketFactorQuoteIdVo(mfId);
        for (Map.Entry<Calendar, Double> quote : quotes.entrySet()) {
            registry.getMarketFactorQuoteEndpoint()
                    .deleteSnapshot().invoke(
                            new HistorizedMarketFactorQuoteIdVo(quote.getKey().getTimeInMillis(), mfQuoteIdVo),
                            new VoidListener() {
                        @Override
                        public void onSuccess() {
                            LOGGER.info("Deleted quote");
                            deleteQuotesLatch.countDown();
                        }

                        @Override
                        public void onError(ErrorVo ev) {
                            errors.add(ev);
                            deleteQuotesLatch.countDown();
                        }

                        @Override
                        public void onNotify(ServiceStatusVo n) {
                            LOGGER.log(Level.INFO, "\n Notify delete quote.\n Status:{0}\n State:{1}\n",
                                    new Object[]{n.getDescription(), n.getState().value()});
                        }
                    });
        }

        deleteQuotesLatch.await();
    }

}
