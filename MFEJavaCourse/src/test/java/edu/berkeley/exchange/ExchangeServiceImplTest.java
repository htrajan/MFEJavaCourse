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
import edu.berkeley.exchange.trader.HoldingKey;
import edu.berkeley.exchange.trader.HoldingRepository;
import edu.berkeley.exchange.trader.Trader;
import edu.berkeley.exchange.trader.TraderRepository;

@ContextConfiguration(classes=TestConfig.class)
public class ExchangeServiceImplTest extends AbstractTestNGSpringContextTests
{
	private static final double STARTING_CAPITAL = 10000.0;
	
	private static final int QTY_1 = 50;
	private static final int QTY_2 = 25;
	private static final int QTY_3 = 75;
	
	private static final double PRICE_1 = 75.0;
	private static final double PRICE_2 = 80.0;
	
	private static final String MS = "Morgan Stanley";
	private static final String GS = "Goldman Sachs";
	private static final String IBM = "IBM";
	private static final String AAPL = "AAPL";
	
	private static final String VALID_ORDER_FAILURE = "Valid order encountered OrderExecutionException.";
	
	private static final String SELL_EXCEPTION_MESSAGE_1 = "Could not execute SELL order on AAPL for Goldman Sachs "
			+ "since price or quantity requested <= 0.";
	private static final String SELL_EXCEPTION_MESSAGE_2 = "Could not execute SELL order on IBM for Goldman Sachs "
			+ "since security is not held.";
	private static final String SELL_EXCEPTION_MESSAGE_3 = "Could not execute SELL order on AAPL for Goldman Sachs "
			+ "due to insufficient quantity of shares.";
	private static final String BUY_EXCEPTION_MESSAGE_1 = "Could not execute BUY order on AAPL for Goldman Sachs "
			+ "since price or quantity requested <= 0.";
	private static final String BUY_EXCEPTION_MESSAGE_2 = "Could not execute BUY order on AAPL for Goldman Sachs "
			+ "due to insufficient capital.";

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
		Stock aapl = new Stock(AAPL, "Apple Computer");
		Stock ibm = new Stock(IBM, "International Business Machines");
		
		stockRepo.save(aapl);
		stockRepo.save(ibm);
		
		Trader gs = new Trader(GS, STARTING_CAPITAL);
		Trader ms = new Trader(MS, STARTING_CAPITAL);
		
		traderRepo.save(gs);
		traderRepo.save(ms);
		
		Holding gsAapl = new Holding(gs, aapl, 100);
		Holding msIbm = new Holding(ms, ibm, 100);
		
		holdingRepo.save(gsAapl);
		holdingRepo.save(msIbm);
	}
	
	@Test(priority = 1)
	public void ordersShouldHaveValidPriceAndQuantity()
	{
		Trader gs = traderRepo.findOne(GS);
		Stock aapl = stockRepo.findOne(AAPL);
		
		try
		{
			exchangeService.placeOrder(gs, aapl, 0, 100, OrderType.BUY);
			fail("Buy order failed to throw OrderExecutionException on bad Price.");
		}
		catch (OrderExecutionException oee)
		{
			assertEquals(oee.getMessage(), BUY_EXCEPTION_MESSAGE_1);
		}
		
		try
		{
			exchangeService.placeOrder(gs, aapl, 100, -1, OrderType.BUY);
			fail("Buy order failed to throw OrderExecutionException on bad Quantity.");
		}
		catch (OrderExecutionException oee)
		{
			assertEquals(oee.getMessage(), BUY_EXCEPTION_MESSAGE_1);
		}
		
		try
		{
			exchangeService.placeOrder(gs, aapl, -1, 100, OrderType.SELL);
			fail("Sell order failed to throw OrderExecutionException on bad Price.");
		}
		catch (OrderExecutionException oee)
		{
			assertEquals(oee.getMessage(), SELL_EXCEPTION_MESSAGE_1);
		}
		
		try
		{
			exchangeService.placeOrder(gs, aapl, 100, 0, OrderType.SELL);
			fail("Sell order failed to throw OrderExecutionException on bad Quantity.");
		}
		catch (OrderExecutionException oee)
		{
			assertEquals(oee.getMessage(), SELL_EXCEPTION_MESSAGE_1);
		}
	}
	
	@Test(priority = 1)
	public void traderShouldHaveSufficientCapitalForBuyOrder()
	{
		Trader gs = traderRepo.findOne(GS);
		Stock aapl = stockRepo.findOne(AAPL);
		
		try
		{
			exchangeService.placeOrder(gs, aapl, 101, 100, OrderType.BUY);
			fail("Buy order failed to throw OrderExecutionException on insufficient capital.");
		}
		catch (OrderExecutionException oee)
		{
			assertEquals(oee.getMessage(), BUY_EXCEPTION_MESSAGE_2);
		}
	}
	
	@Test(priority = 1)
	public void traderShouldHaveCorrectHoldingForSellOrder()
	{
		Trader gs = traderRepo.findOne(GS);
		Stock ibm = stockRepo.findOne(IBM);
		
		try
		{
			exchangeService.placeOrder(gs, ibm, 100, 100, OrderType.SELL);
			fail("Sell order failed to throw OrderExecutionException on security not held.");
		}
		catch (OrderExecutionException oee)
		{
			assertEquals(oee.getMessage(), SELL_EXCEPTION_MESSAGE_2);
		}
	}
	
	@Test(priority = 1)
	public void traderShouldHaveSufficientQuantityForSellOrder()
	{
		Trader gs = traderRepo.findOne(GS);
		Stock aapl = stockRepo.findOne(AAPL);
		
		try
		{
			exchangeService.placeOrder(gs, aapl, 100, 101, OrderType.SELL);
			fail("Sell order failed to throw OrderExecutionException on insufficient quantity of shares.");
		}
		catch (OrderExecutionException oee)
		{
			assertEquals(oee.getMessage(), SELL_EXCEPTION_MESSAGE_3);
		}
	}
	
	@Test(priority = 2)
	public void newOrderShouldPopulateCorrectly()
	{
		Trader gs = traderRepo.findOne(GS);
		
		Stock aapl = stockRepo.findOne(AAPL);
		Stock ibm = stockRepo.findOne(IBM);
		
		try
		{
			exchangeService.placeOrder(gs, ibm, PRICE_1, QTY_1, OrderType.BUY); //1
			exchangeService.placeOrder(gs, aapl, PRICE_1, QTY_1, OrderType.SELL); //2
			
			Order buyIbm = exchangeService.getBestBid(ibm);
			verifyOrder(buyIbm, gs, ibm, PRICE_1, QTY_1, OrderType.BUY);
			
			Order sellAapl = exchangeService.getBestAsk(aapl);
			verifyOrder(sellAapl, gs, aapl, PRICE_1, QTY_1, OrderType.SELL);
		}
		catch (OrderExecutionException oee)
		{
			fail(VALID_ORDER_FAILURE);
		}
	}
	
	@Test(dependsOnMethods="newOrderShouldPopulateCorrectly")
	public void openBuyOrderShouldDepleteCapital()
	{
		Trader gs = traderRepo.findOne(GS);
		assertEquals(gs.getCapital(), 6250.0);
	}
	
	@Test(dependsOnMethods="newOrderShouldPopulateCorrectly")
	public void openSellOrderShouldDepleteHoldings()
	{
		Trader gs = traderRepo.findOne(GS);
		Stock aapl = stockRepo.findOne(AAPL);
		
		HoldingKey holdingKey = new HoldingKey(gs.getName(), aapl.getTicker());
		Holding holding = holdingRepo.findOne(holdingKey);
		
		assertEquals(holding.getQuantity(), 50);
	}
	
	@Test(dependsOnMethods="openSellOrderShouldDepleteHoldings")
	public void sellOffOfHoldingShouldEraseIt()
	{
		Trader gs = traderRepo.findOne(GS);
		Stock aapl = stockRepo.findOne(AAPL);
		
		try
		{
			exchangeService.placeOrder(gs, aapl, PRICE_2, QTY_1, OrderType.SELL); //3
		}
		catch (OrderExecutionException oee)
		{
			fail(VALID_ORDER_FAILURE);
		}
		
		HoldingKey holdingKey = new HoldingKey(gs.getName(), aapl.getTicker());
		Holding holding = holdingRepo.findOne(holdingKey);
		
		assertNull(holding);
	}
	
	@Test(dependsOnMethods="sellOffOfHoldingShouldEraseIt")
	public void buyOrderShouldExecuteAtBestAvailablePrice()
	{
		Trader ms = traderRepo.findOne(MS);
		Trader gs = traderRepo.findOne(GS);
		
		Stock aapl = stockRepo.findOne(AAPL);
		
		try
		{
			exchangeService.placeOrder(ms, aapl, PRICE_2, QTY_2, OrderType.BUY); //4, 5
		}
		catch (OrderExecutionException oee)
		{
			fail(VALID_ORDER_FAILURE);
		}
		
		Order lastExecutedBuy = exchangeService.getLastExecutedBuy(aapl, ms);
		verifyOrder(lastExecutedBuy, ms, aapl, PRICE_1, QTY_2, OrderType.BUY);
		
		Order lastExecutedSell = exchangeService.getLastExecutedSell(aapl, gs);
		verifyOrder(lastExecutedSell, gs, aapl, PRICE_1, QTY_2, OrderType.SELL);
		
		Order matchingSell = orderRepo.findOne(2L);
		verifyOrder(matchingSell, gs, aapl, PRICE_1, QTY_2, OrderType.SELL);
		assertFalse(matchingSell.isExecuted());
		
		ms = traderRepo.findOne(MS);
		assertEquals(ms.getCapital(), STARTING_CAPITAL - PRICE_1 * QTY_2);
		
		double capital = gs.getCapital();
		gs = traderRepo.findOne(GS);
		assertEquals(gs.getCapital(), capital + PRICE_1 * QTY_2);
		
		HoldingKey holdingKey = new HoldingKey(ms.getName(), aapl.getTicker());
		Holding holding = holdingRepo.findOne(holdingKey);
		assertNotNull(holding);
		assertEquals(holding.getQuantity(), QTY_2);
	}
	
	@Test(dependsOnMethods="buyOrderShouldExecuteAtBestAvailablePrice")
	public void buyOrderShouldBeAbleToFillAcrossMultipleSells()
	{
		Trader ms = traderRepo.findOne(MS);
		Trader gs = traderRepo.findOne(GS);
		
		double msCapital = ms.getCapital();
		double gsCapital = gs.getCapital();
		
		Stock aapl = stockRepo.findOne(AAPL);
		
		try
		{
			exchangeService.placeOrder(ms, aapl, PRICE_2, QTY_3, OrderType.BUY); //6, 7, 8
		}
		catch (OrderExecutionException oee)
		{
			fail(VALID_ORDER_FAILURE);
		}
		
		Order lastExecutedBuy = exchangeService.getLastExecutedBuy(aapl, ms);
		verifyOrder(lastExecutedBuy, ms, aapl, PRICE_2, QTY_1, OrderType.BUY);
		
		Order lastExecutedSell = exchangeService.getLastExecutedSell(aapl, gs);
		verifyOrder(lastExecutedSell, gs, aapl, PRICE_1, QTY_2, OrderType.SELL);
		
		Order previousBuy = orderRepo.findOne(6L);
		verifyOrder(previousBuy, ms, aapl, PRICE_1, QTY_2, OrderType.BUY);
		
		Order matchingSell1 = orderRepo.findOne(2L);
		assertTrue(matchingSell1.isExecuted());
		
		Order matchingSell2 = orderRepo.findOne(3L);
		assertTrue(matchingSell2.isExecuted());
		
		ms = traderRepo.findOne(MS);
		assertEquals(ms.getCapital(), msCapital - (PRICE_2 * QTY_1) - (PRICE_1 * QTY_2));
		
		gs = traderRepo.findOne(GS);
		assertEquals(gs.getCapital(), gsCapital + (PRICE_2 * QTY_1) + (PRICE_1 * QTY_2));
		
		HoldingKey holdingKey = new HoldingKey(ms.getName(), aapl.getTicker());
		Holding holding = holdingRepo.findOne(holdingKey);
		assertNotNull(holding);
		assertEquals(holding.getQuantity(), QTY_2 + QTY_3);
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
