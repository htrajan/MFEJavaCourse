package edu.berkeley.exchange;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.berkeley.exchange.order.OrderRepository;
import edu.berkeley.exchange.security.StockRepository;
import edu.berkeley.exchange.trader.HoldingRepository;
import edu.berkeley.exchange.trader.TraderRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=TestConfig.class)
public class ExchangeServiceImplTest 
{
	@Autowired
	private StockRepository stockRepo;
	
	@Autowired
	private HoldingRepository holdingRepo;
	
	@Autowired
	private OrderRepository orderRepo;
	
	@Autowired
	private TraderRepository traderRepo;
	
	private ExchangeServiceImpl exchangeService;
	
	@Before
	public void setUp()
	{
		exchangeService = new ExchangeServiceImpl(orderRepo, holdingRepo, traderRepo);
	}
}
