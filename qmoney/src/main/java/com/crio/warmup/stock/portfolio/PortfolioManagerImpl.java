
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import java.text.MessageFormat;
import com.crio.warmup.stock.PortfolioManagerApplication;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {


  RestTemplate restTemplate;
  StockQuotesService stockQuotesService;

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility

  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  protected PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotesService = stockQuotesService;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF

  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) {
    List<Candle> candles;
    ArrayList<AnnualizedReturn> annualizedReturns = new ArrayList<>();
    for (PortfolioTrade trade : portfolioTrades) {
      try {
        candles = this.stockQuotesService.getStockQuote(trade.getSymbol(), trade.getPurchaseDate(), endDate);
        AnnualizedReturn annualizedReturn = calculateAnnualizedReturnsPerTrade(endDate, trade, 
        PortfolioManagerApplication.getOpeningPriceOnStartDate(candles), PortfolioManagerApplication.getClosingPriceOnEndDate(candles));
        annualizedReturns.add(annualizedReturn);
      } catch (JsonProcessingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    Collections.sort(annualizedReturns, getComparator());
    return annualizedReturns;
  }

  public AnnualizedReturn calculateAnnualizedReturnsPerTrade(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
        double totReturns = (sellPrice - buyPrice)/buyPrice;
        double year = trade.getPurchaseDate().until(endDate, ChronoUnit.DAYS)/364.24;
        double annReturns = Math.pow(1 + totReturns, 1 / year) - 1;
        return new AnnualizedReturn(trade.getSymbol(), annReturns, totReturns);
  }


  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
    // RestTemplate restTemplate = new RestTemplate();
    return stockQuotesService.getStockQuote(symbol, from, to);
    // TiingoCandle[] candles;
    // List<Candle> candleList = new ArrayList<>();
    // candles = this.restTemplate.getForObject(
    //   buildUri(symbol, from, to),
    //     TiingoCandle[].class);
    // for (TiingoCandle candle : candles) {
    //   // Add each element into the list
    //   candleList.add(candle);
    // }
    // return candleList;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
       String uriTemplateWithTokens = "https://api.tiingo.com/tiingo/daily/{0}/prices?"
            + "startDate={1}&endDate={2}&token={3}";
      String uriTemplate = MessageFormat.format(uriTemplateWithTokens, symbol, startDate, endDate, PortfolioManagerApplication.getToken());
      return uriTemplate;
  }


  // ¶TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Modify the function #getStockQuote and start delegating to calls to
  //  stockQuoteService provided via newly added constructor of the class.
  //  You also have a liberty to completely get rid of that function itself, however, make sure
  //  that you do not delete the #getStockQuote function.

}
