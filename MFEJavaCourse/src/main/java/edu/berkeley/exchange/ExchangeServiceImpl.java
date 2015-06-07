package edu.berkeley.exchange;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.berkeley.exchange.order.Order;
import edu.berkeley.exchange.order.Order.OrderType;
import edu.berkeley.exchange.order.OrderRepository;
import edu.berkeley.exchange.security.Security;
import edu.berkeley.exchange.trader.Holding;
import edu.berkeley.exchange.trader.HoldingKey;
import edu.berkeley.exchange.trader.HoldingRepository;
import edu.berkeley.exchange.trader.Trader;
import edu.berkeley.exchange.trader.TraderRepository;

@Service
public class ExchangeServiceImpl implements ExchangeService 
{
	private OrderRepository orderRepo;
	private HoldingRepository holdingRepo;
	private TraderRepository traderRepo;
	
	@Autowired
	public ExchangeServiceImpl(OrderRepository orderRepo, HoldingRepository holdingRepo, 
			TraderRepository traderRepo)
	{
		this.orderRepo = orderRepo;
		this.holdingRepo = holdingRepo;
		this.traderRepo = traderRepo;
	}

	@Override
	public Order getBestBid(Security security) 
	{
		return orderRepo.findTopBySecurityAndTypeAndExecutedOrderByPriceDesc(security, 
				OrderType.BUY, false);
	}

	@Override
	public Order getBestAsk(Security security) 
	{
		return orderRepo.findTopBySecurityAndTypeAndExecutedOrderByPriceAsc(security, 
				OrderType.SELL, false);
	}
	
	@Override
	public void placeOrder(Trader trader, Security security, double price,
			int quantity, OrderType type)
			throws OrderExecutionException
	{
		if (type.equals(OrderType.SELL))
		{
			placeSellOrder(trader, security, price, quantity);
		}
		else
		{
			placeBuyOrder(trader, security, price, quantity);
		}
	}
	
	private void placeBuyOrder(Trader trader, Security security, double price,
			int quantity) throws OrderExecutionException
	{
		if (price <= 0 || quantity <= 0)
		{
			throw new OrderExecutionException(
					"Could not execute BUY order on " + security.getTicker() +
					" for " + trader.getName() + " since price or quantity requested <= 0.");
		}
		double capitalRequired = price * quantity;
		if (capitalRequired > trader.getCapital())
		{
			throw new OrderExecutionException(
					"Could not execute BUY order on " + security.getTicker() +
					" for " + trader.getName() + " due to insufficient capital.");
		}
		else
		{
			double totalCost = 0;
			
			Order matchingSell = getBestAsk(security);
			while (matchingSell != null && matchingSell.getPrice() <= price && quantity > 0)
			{
				int sellQuantity = matchingSell.getQuantity();
				double sellPrice = matchingSell.getPrice();
				
				Trader sellTrader = matchingSell.getTrader();
				double sellTraderCapital = sellTrader.getCapital();
				
				if (sellQuantity <= quantity)
				{
					quantity -= sellQuantity;
					
					double saleAmount = sellPrice * sellQuantity;
					
					sellTrader.setCapital(sellTraderCapital + saleAmount);
					traderRepo.save(sellTrader);
					
					totalCost += saleAmount;
					
					matchingSell.setExecuted(true);
					orderRepo.save(matchingSell);
					
					if (quantity > 0)
					{
						matchingSell = getBestAsk(security);
					}
				}
				else
				{
					Order order = new Order(security, trader, price, quantity, OrderType.BUY);
					order.setExecuted(true);
					orderRepo.save(order);
					
					sellQuantity -= quantity;
					
					double saleAmount = sellPrice * quantity;
					
					sellTrader.setCapital(sellTraderCapital + saleAmount);
					traderRepo.save(sellTrader);
					
					totalCost += saleAmount;
					quantity = 0;
					
					matchingSell.setQuantity(sellQuantity);
					orderRepo.save(matchingSell);
				}
				
				if (totalCost > 0)
				{
					double capital = trader.getCapital();
					trader.setCapital(capital - totalCost);
					traderRepo.save(trader);
				}
				
				if (quantity > 0)
				{
					Order order = new Order(security, trader, price, quantity, OrderType.BUY);
					orderRepo.save(order);
				}
			}
		}
	}

	private void placeSellOrder(Trader trader, Security security, double price,
			int quantity) throws OrderExecutionException 
	{
		HoldingKey holdingKey = new HoldingKey(trader, security);
		Holding holding = holdingRepo.findOne(holdingKey);
		
		if (price <= 0 || quantity <= 0)
		{
			throw new OrderExecutionException(
					"Could not execute SELL order on " + security.getTicker() +
					" for " + trader.getName() + " since price or quantity requested <= 0.");
		}
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
			
			double saleProceeds = 0;
			
			Order matchingBuy = getBestBid(security);
			while (matchingBuy != null && matchingBuy.getPrice() >= price && quantity > 0)
			{
				int buyQuantity = matchingBuy.getQuantity();
				double buyPrice = matchingBuy.getPrice();
				
				Trader buyTrader = matchingBuy.getTrader();
				double buyTraderCapital = buyTrader.getCapital();
				
				HoldingKey buyerHoldingKey = new HoldingKey(buyTrader, security);
				Holding buyerHolding = holdingRepo.findOne(buyerHoldingKey);
				
				if (buyQuantity <= quantity)
				{
					quantity -= buyQuantity;
					
					if (buyerHolding != null)
					{
						int buyerHoldingQuantity = buyerHolding.getQuantity();
						buyerHolding.setQuantity(buyerHoldingQuantity + buyQuantity);
						holdingRepo.save(buyerHolding);
					}
					else
					{
						buyerHolding = new Holding(buyTrader, security, buyQuantity);
						holdingRepo.save(buyerHolding);
					}
					
					double saleAmount = buyPrice * buyQuantity;
					buyTrader.setCapital(buyTraderCapital - saleAmount);
					traderRepo.save(buyTrader);
					
					saleProceeds += saleAmount;
					
					matchingBuy.setExecuted(true);
					orderRepo.save(matchingBuy);
					
					if (quantity > 0)
					{
						matchingBuy = getBestBid(security);
					}
				}
				else
				{
					if (buyerHolding != null)
					{
						int buyerHoldingQuantity = buyerHolding.getQuantity();
						buyerHolding.setQuantity(buyerHoldingQuantity + quantity);
						holdingRepo.save(buyerHolding);
					}
					else
					{
						buyerHolding = new Holding(buyTrader, security, quantity);
						holdingRepo.save(buyerHolding);
					}
					
					Order order = new Order(security, trader, price, quantity, OrderType.SELL);
					order.setExecuted(true);
					orderRepo.save(order);
					
					buyQuantity -= quantity;
					
					double saleAmount = buyPrice * quantity;
					buyTrader.setCapital(buyTraderCapital - saleAmount);
					traderRepo.save(buyTrader);
					
					saleProceeds += buyPrice * quantity;
					quantity = 0;
					
					matchingBuy.setQuantity(buyQuantity);
					orderRepo.save(matchingBuy);
				}
			}
			
			if (saleProceeds > 0)
			{
				double capital = trader.getCapital();
				trader.setCapital(capital + saleProceeds);
				traderRepo.save(trader);
			}
			
			if (quantity > 0)
			{
				Order order = new Order(security, trader, price, quantity, OrderType.SELL);
				orderRepo.save(order);
			}
		}
	}
}