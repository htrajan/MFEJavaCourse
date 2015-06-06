package edu.berkeley.exchange.trader;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

import edu.berkeley.exchange.security.Security;

@Embeddable
public class HoldingKey implements Serializable
{
	private static final long serialVersionUID = -3562898376349687851L;

	@ManyToOne
	private Trader trader;
	
	private Security security;
	
	public HoldingKey(Trader trader, Security security) 
	{
		this.trader = trader;
		this.security = security;
	}
	
	public Trader getTrader() {
		return trader;
	}
	public void setTrader(Trader trader) {
		this.trader = trader;
	}
	public Security getSecurity() {
		return security;
	}
	public void setSecurity(Security security) {
		this.security = security;
	}
}
