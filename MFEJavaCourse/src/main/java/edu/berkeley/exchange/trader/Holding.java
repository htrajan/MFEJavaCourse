package edu.berkeley.exchange.trader;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class Holding 
{
	@EmbeddedId
	private HoldingKey key;
	
	private int quantity;

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
