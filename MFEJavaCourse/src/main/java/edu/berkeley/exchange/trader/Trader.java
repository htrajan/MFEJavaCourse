package edu.berkeley.exchange.trader;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Trader 
{
	@Id
	private String name;
	
	private double capital;
	
	protected Trader()
	{
		
	}
	
	public Trader(String name, double capital)
	{
		this.name = name;
		this.capital = capital;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getCapital() {
		return capital;
	}

	public void setCapital(double capital) {
		this.capital = capital;
	}
}
