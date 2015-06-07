package edu.berkeley.exchange.trader;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import edu.berkeley.exchange.security.Security;

@Entity
public class Holding 
{
	@EmbeddedId
	private HoldingKey key;
	
	private int quantity;
	
	public Holding(Trader trader, Security security, int quantity)
	{
		HoldingKey key = new HoldingKey(trader, security);
		this.key = key;
		this.quantity = quantity;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
