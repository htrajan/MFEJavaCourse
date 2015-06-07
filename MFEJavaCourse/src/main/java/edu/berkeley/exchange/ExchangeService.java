package edu.berkeley.exchange;

import edu.berkeley.exchange.order.Order.OrderType;
import edu.berkeley.exchange.security.Security;
import edu.berkeley.exchange.trader.Trader;

public interface ExchangeService 
{
	public void placeOrder(Trader trader, 
						   Security security,
						   double price,
						   int quantity,
						   OrderType type)
						   throws OrderExecutionException;
}
