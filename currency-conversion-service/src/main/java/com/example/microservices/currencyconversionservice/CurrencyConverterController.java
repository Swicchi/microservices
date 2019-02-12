package com.example.microservices.currencyconversionservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
public class CurrencyConverterController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CurrencyExchangeServiceProxy currencyExchangeServiceProxy;

    @Autowired
    public CurrencyConverterController(CurrencyExchangeServiceProxy currencyExchangeServiceProxy) {
        this.currencyExchangeServiceProxy = currencyExchangeServiceProxy;
    }

    @GetMapping("currency-converter/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversionBean convertCurrency(@PathVariable String from,
                                                  @PathVariable String to,
                                                  @PathVariable BigDecimal quantity){
        Map<String,String> uriVariables = new HashMap<>();
        uriVariables.put("from",from);
        uriVariables.put("to",to);
        ResponseEntity<CurrencyConversionBean> forEntity = new RestTemplate().
                getForEntity("http://localhost:8000/currency-exchange/from/{from}/to/{to}", CurrencyConversionBean.class, uriVariables);
        BigDecimal conversionMultiple = Objects.requireNonNull(forEntity.getBody()).getConversionMultiple();
        Long id = Objects.requireNonNull(forEntity.getBody()).getId();
        int port = Objects.requireNonNull(forEntity.getBody()).getPort();
        return new CurrencyConversionBean(id,from,to, conversionMultiple,quantity,quantity.multiply(conversionMultiple),port);
    }

    @GetMapping("currency-converter-feign/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversionBean convertCurrencyFeign(@PathVariable String from,
                                                  @PathVariable String to,
                                                  @PathVariable BigDecimal quantity){
        //Feign - P1
        CurrencyConversionBean currencyConversionBean = currencyExchangeServiceProxy.retrieveExchangeValue(from, to);
        logger.info("{}",currencyConversionBean);
        return new CurrencyConversionBean(currencyConversionBean.getId(),from,to, currencyConversionBean.getConversionMultiple(),
                quantity,quantity.multiply(currencyConversionBean.getConversionMultiple()),currencyConversionBean.getPort());
    }

}
