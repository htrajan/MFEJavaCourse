package edu.berkeley.exchange;

import static org.testng.Assert.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import edu.berkeley.exchange.order.Order;
import edu.berkeley.exchange.order.Order.OrderType;
import edu.berkeley.exchange.order.OrderRepository;
import edu.berkeley.exchange.security.Security;
import edu.berkeley.exchange.security.Stock;
import edu.berkeley.exchange.security.StockRepository;
import edu.berkeley.exchange.trader.Holding;
import edu.berkeley.exchange.trader.HoldingRepository;
import edu.berkeley.exchange.trader.Trader;
import edu.berkeley.exchange.trader.TraderRepository;

@ContextConfiguration(classes=TestConfig.class)
public class ExchangeServiceImplTest extends AbstractTestNGSpringContextTests
{
	private static final String VALID_ORDER_FAILURE = "Valid order encountered OrderExecutionException.";

	private static final String SELL_EXCEPTION_MESSAGE = "Could not execute SELL order on AAPL for Goldman Sachs "
			+ "since price or quantity requested <= 0.";

	private static final String BUY_EXCEPTION_MESSAGE = "Could not execute BUY order on AAPL for Goldman Sachs "
			+ "since price or quantity requested <= 0.";

	@Autowired
	private StockRepository stockRepo;
	
	@Autowired
	private HoldingRepository holdingRepo;
	
	@Autowired
	private OrderRepository orderRepo;
	
	@Autowired
	private TraderRepository traderRepo;
	
	private ExchangeServiceImpl exchangeService;
	
	@BeforeClass
	public void setUp()
	{
		setUpService();
		setUpData();
	}
	
	private void setUpService()
	{
		exchangeService = new ExchangeServiceImpl(orderRepo, holdingRepo, traderRepo);
	}
	
	private void setUpData()
	{
		Stock aapl = new Stock("AAPL", "Apple Computer");
		Stock ibm = new Stock("IBM", "International Business Machines");
		
		stockRepo.save(aapl);
		stockRepo.save(ibm);
		
		Trader gs = new Trader("Goldman Sachs", 10000);
		Trader ms = new Trader("Morgan Stanley", 5000);
		
		traderRepo.save(gs);
		traderRepo.save(ms);
		
		Holding gsAapl = new Holding(gs, aapl, 100);
		Holding msIbm = new Holding(ms, ibm, 100);
		
		holdingRepo.save(gsAapl);
		holdingRepo.save(msIbm);
	}
	
	@Test
	public void ordersShouldHaveValidPriceAndQuantity()
	{
		Trader gs = traderRepo.findOne("Goldman Sachs");
		
		Stock aapl = stockRepo.findOne("AAPL");
		
		try
		{
			exchangeService.placeOrder(gs, aapl, 0, 100, OrderType.BUY);
			fail("[BUY] Failed to throw OrderExecutionException on bad Price.");
		}
		catch (OrderExecutionException oee)
		{
			assertEquals(oee.getMessage(), BUY_EXCEPTION_MESSAGE);
		}
		
		try
		{
			exchangeService.placeOrder(gs, aapl, 100, -1, OrderType.BUY);
			fail("[BUY] Failed to throw OrderExecutionException on bad Quantity.");
		}
		catch (OrderExecutionException oee)
		{
			assertEquals(oee.getMessage(), BUY_EXCEPTION_MESSAGE);
		}
		
		try
		{
			exchangeService.placeOrder(gs, aapl, -1, 100, OrderType.SELL);
			fail("[SELL] Failed to throw OrderExecutionException on bad Price.");
		}
		catch (OrderExecutionException oee)
		{
			assertEquals(oee.getMessage(), SELL_EXCEPTION_MESSAGE);
		}
		
		try
		{
			exchangeService.placeOrder(gs, aapl, 100, 0, OrderType.SELL);
			fail("[SELL] Failed to throw OrderExecutionException on bad Quantity.");
		}
		catch (OrderExecutionException oee)
		{
			assertEquals(oee.getMessage(), SELL_EXCEPTION_MESSAGE);
		}
	}
	
	@Test
	public void newOrderShouldPopulateCorrectly()
	{
		Trader gs = traderRepo.findOne("Goldman Sachs");
		
		Stock aapl = stockRepo.findOne("AAPL");
		Stock ibm = stockRepo.findOne("IBM");
		
		try
		{
			exchangeService.placeOrder(gs, ibm, 100, 100, OrderType.BUY);
			exchangeService.placeOrder(gs, aapl, 100, 100, OrderType.SELL);
			
			Order buyIbm = exchangeService.getBestBid(ibm);
			verifyOrder(buyIbm, gs, ibm, 100, 100, OrderType.BUY);
			
			Order sellAapl = exchangeService.getBestAsk(aapl);
			verifyOrder(sellAapl, gs, aapl, 100, 100, OrderType.SELL);
		}
		catch (OrderExecutionException oee)
		{
			fail(VALID_ORDER_FAILURE);
		}
	}
	
	private void verifyOrder(Order order, Trader trader, Security security, double price, int quantity, OrderType type)
	{
		assertNotNull(order);
		assertEquals(order.getTrader().getName(), trader.getName());
		assertEquals(order.getSecurity().getTicker(), security.getTicker());
		assertEquals(order.getPrice(), price);
		assertEquals(order.getQuantity(), quantity);
		assertEquals(order.getType(), type);
	}
}
