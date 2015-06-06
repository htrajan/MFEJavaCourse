package edu.berkeley.exchange;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.berkeley.exchange.order.Order.OrderExecution;
import edu.berkeley.exchange.order.Order.OrderType;
import edu.berkeley.exchange.order.OrderRepository;
import edu.berkeley.exchange.security.Security;
import edu.berkeley.exchange.security.StockRepository;
import edu.berkeley.exchange.trader.Holding;
import edu.berkeley.exchange.trader.HoldingKey;
import edu.berkeley.exchange.trader.HoldingRepository;
import edu.berkeley.exchange.trader.Trader;
import edu.berkeley.exchange.trader.TraderRepository;

@Service
public class ExchangeServiceImpl implements ExchangeService 
{
	@Autowired
	OrderRepository orderRepo;
	
	@Autowired
	StockRepository stockRepo;
	
	@Autowired
	HoldingRepository holdingRepo;
	
	@Autowired
	TraderRepository traderRepo;
	
	@Override
	public void placeOrder(Trader trader, Security security, double price,
			int quantity, OrderType type, OrderExecution execution)
			throws OrderExecutionException
	{
		if (type.equals(OrderType.SELL))
		{
			HoldingKey holdingKey = new HoldingKey(trader, security);
			Holding holding = holdingRepo.findOne(holdingKey);
			
			if (holding == null)
			{
				throw new OrderExecutionException(
						"Could not execute SELL order on " + security.getTicker() +
						" for " + trader.getName() + " since security is not held.");
			}
			else if (holding.getQuantity() < quantity)
			{
				throw new OrderExecutionException(
						"Could not execute SELL order on " + security.getTicker() +
						" for " + trader.getName() + " due to insufficient quantity of shares.");
			}
			else
			{
				int quantityHeld = holding.getQuantity();
				if (quantityHeld == quantity)
				{
					holdingRepo.delete(holding);
				}
				else
				{
					holding.setQuantity(quantityHeld - quantity);
					holdingRepo.save(holding);
				}
				
				if (execution.equals(OrderExecution.MARKET))
				{
					
				}
			}
		}
	}

}
