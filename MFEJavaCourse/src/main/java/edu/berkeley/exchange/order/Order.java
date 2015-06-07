package edu.berkeley.exchange.order;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import edu.berkeley.exchange.security.Security;
import edu.berkeley.exchange.trader.Trader;

@Entity
@Table(name="ORDERS")
public class Order 
{
	public enum OrderType {
		BUY,
		SELL
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	@Column(insertable=false, updatable=false)
	private String ticker;
	
	@ManyToOne(optional=false)
	@JoinColumn(name="ticker", referencedColumnName="ticker")
	private Security security;
	
	@Column(insertable=false, updatable=false)
	private String traderName;
	
	@ManyToOne(optional=false)
	@JoinColumn(name="traderName", referencedColumnName="name")
	private Trader trader;
	
	private double price;
	private int quantity;
	
	@Enumerated(EnumType.STRING)
	private OrderType type;
	
	private boolean executed;
	
	protected Order()
	{
		
	}
	
	public Order(Security security, Trader trader, double price, int quantity,
			OrderType type) 
	{
		this.security = security;
		this.ticker = security.getTicker();
		this.trader = trader;
		this.traderName = trader.getName();
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
	
	public String getTicker() {
		return ticker;
	}

	public void setSecurity(Security security) {
		this.security = security;
		this.ticker = security.getTicker();
	}

	public Trader getTrader() {
		return trader;
	}
	
	public String getTraderName() {
		return traderName;
	}

	public void setTrader(Trader trader) {
		this.trader = trader;
		this.traderName = trader.getName();
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
