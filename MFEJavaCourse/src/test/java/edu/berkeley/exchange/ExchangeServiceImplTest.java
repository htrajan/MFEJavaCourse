package edu.berkeley.exchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import edu.berkeley.exchange.order.Order.OrderType;
import edu.berkeley.exchange.order.OrderRepository;
import edu.berkeley.exchange.security.Stock;
import edu.berkeley.exchange.security.StockRepository;
import edu.berkeley.exchange.trader.Holding;
import edu.berkeley.exchange.trader.HoldingRepository;
import edu.berkeley.exchange.trader.Trader;
import edu.berkeley.exchange.trader.TraderRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=TestConfig.class, loader=AnnotationConfigContextLoader.class)
@ComponentScan(basePackages="edu.berkeley.exchange")
public class ExchangeServiceImplTest
{
	@Autowired
	private static StockRepository stockRepo;
	
	@Autowired
	private static HoldingRepository holdingRepo;
	
	@Autowired
	private static OrderRepository orderRepo;
	
	@Autowired
	private static TraderRepository traderRepo;
	
	private static ExchangeServiceImpl exchangeService;
	
	@BeforeClass
	public static void setUp()
	{
		setUpService();
		setUpData();
	}
	
	private static void setUpService()
	{
		exchangeService = new ExchangeServiceImpl(orderRepo, holdingRepo, traderRepo);
	}
	
	private static void setUpData()
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
			fail("Failed to throw OrderExecutionException on bad Price.");
		}
		catch (OrderExecutionException oee)
		{
			assertEquals("Could not place BUY order on AAPL for Goldman Sachs "
					+ "since price or quantity requested <= 0.", oee.getMessage());
		}
		
		try
		{
			exchangeService.placeOrder(gs, aapl, 100, -1, OrderType.BUY);
			fail("Failed to throw OrderExecutionException on bad Quantity.");
		}
		catch (OrderExecutionException oee)
		{
			assertEquals("Could not place BUY order on AAPL for Goldman Sachs "
					+ "since price or quantity requested <= 0.", oee.getMessage());
		}
		
		try
		{
			exchangeService.placeOrder(gs, aapl, -1, 100, OrderType.SELL);
			fail("Failed to throw OrderExecutionException on bad Price.");
		}
		catch (OrderExecutionException oee)
		{
			assertEquals("Could not place SELL order on AAPL for Goldman Sachs "
					+ "since price or quantity requested <= 0.", oee.getMessage());
		}
		
		try
		{
			exchangeService.placeOrder(gs, aapl, 100, 0, OrderType.SELL);
			fail("Failed to throw OrderExecutionException on bad Quantity.");
		}
		catch (OrderExecutionException oee)
		{
			assertEquals("Could not place SELL order on AAPL for Goldman Sachs "
					+ "since price or quantity requested <= 0.", oee.getMessage());
		}
	}
}
