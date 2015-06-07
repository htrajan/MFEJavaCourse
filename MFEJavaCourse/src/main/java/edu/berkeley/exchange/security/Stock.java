package edu.berkeley.exchange.security;

import javax.persistence.Entity;

@Entity
public class Stock extends Security 
{	
	private String companyName;
	
	public Stock(String ticker, String companyName)
	{
		this.ticker = ticker;
		this.companyName = companyName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
}
