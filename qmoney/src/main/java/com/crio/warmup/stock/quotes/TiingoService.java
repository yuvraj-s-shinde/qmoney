
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.PortfolioManagerApplication;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {

  private RestTemplate restTemplate;

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest
  
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
    // RestTemplate restTemplate1 = new RestTemplate();
    ObjectMapper objmapper=new ObjectMapper();
        objmapper.registerModule(new JavaTimeModule());

    Candle[] candles = null;
    List<Candle> candleList = new ArrayList<>();
    String apiResponse = restTemplate.getForObject(
      buildUri(symbol, from, to),
        String.class);

        try {
          candles =objmapper.readValue(apiResponse, TiingoCandle[].class);
        } catch (JsonMappingException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (JsonProcessingException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        
        if(candles==null){
        
          return new ArrayList<>();
          
        }
  
        return Arrays.asList(candles);

  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Write a method to create appropriate url to call the Tiingo API.
  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplateWithTokens = "https://api.tiingo.com/tiingo/daily/{0}/prices?"
         + "startDate={1}&endDate={2}&token={3}";
   String uriTemplate = MessageFormat.format(uriTemplateWithTokens, symbol, startDate, endDate, getToken());
   return uriTemplate;
  }

  public static String getToken() {
    return "8d1051d60d86c0c0346a54396a53d6801c27cc16";
  }
}
