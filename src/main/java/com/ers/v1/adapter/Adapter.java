/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.adapter;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import com.ers.re.service.pub.api.v1_1.messaging.EndpointRegistry;
import com.ers.v1.connector.REConnector;
import com.eurorisksystems.riskengine.ws.v1_1.vo.ErrorVo;
import com.eurorisksystems.riskengine.ws.v1_1.vo.market.factor.InstrumentMarketFactorVo;

/**
 *
 * @author snyanakieva
 */
public class Adapter {

    protected final static Logger LOGGER = Logger.getLogger(Adapter.class.getCanonicalName());
    protected final EndpointRegistry registry = REConnector.INSTANCE.getRegistry();
    protected final Collection<ErrorVo> errors = new ConcurrentLinkedQueue();
    protected InstrumentMarketFactorVo marketFactorVo = null;

    public Collection<ErrorVo> getErrors() {
        return errors;
    }
    
    public InstrumentMarketFactorVo getMarketFactorVo() {
        return marketFactorVo;
    }
}
