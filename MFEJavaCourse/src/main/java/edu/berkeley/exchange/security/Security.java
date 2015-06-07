package edu.berkeley.exchange.security;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public abstract class Security 
{
	@Id
	protected String ticker;
	
	public String getTicker() {
		return ticker;
	}
	
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
}
