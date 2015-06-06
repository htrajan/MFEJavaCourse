package edu.berkeley.exchange.security;

import javax.persistence.Entity;

@Entity
public class Stock extends Security 
{	
	private String companyName;

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
}
