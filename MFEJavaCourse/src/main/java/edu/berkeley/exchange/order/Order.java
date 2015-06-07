package edu.berkeley.exchange.order;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import edu.berkeley.exchange.security.Security;
import edu.berkeley.exchange.trader.Trader;

@Entity
public class Order 
{
	public enum OrderType {
		BUY,
		SELL
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	private Security security;
	private Trader trader;
	
	private double price;
	private int quantity;
	
	@Enumerated(EnumType.STRING)
	private OrderType type;
	
	private boolean executed;
	
	public Order(Security security, Trader trader, double price, int quantity,
			OrderType type) 
	{
		this.security = security;
		this.trader = trader;
		this.price = price;
		this.quantity = quantity;
		this.type = type;
		this.executed = false;
		this.timestamp = new Date(System.currentTimeMillis());
	}

	private Date timestamp;

	public Security getSecurity() {
		return security;
	}

	public void setSecurity(Security security) {
		this.security = security;
	}

	public Trader getTrader() {
		return trader;
	}

	public void setTrader(Trader trader) {
		this.trader = trader;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public OrderType getType() {
		return type;
	}

	public void setType(OrderType type) {
		this.type = type;
	}

	public boolean isExecuted() {
		return executed;
	}

	public void setExecuted(boolean executed) {
		this.executed = executed;
	}

	public Date getTimestamp() {
		return timestamp;
	}
}
