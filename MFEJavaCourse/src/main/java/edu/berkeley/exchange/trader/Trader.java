package edu.berkeley.exchange.trader;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Trader 
{
	@Id
	private String name;
	
	private double capital;
	
	@OneToMany
	private List<Holding> holdings;

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
	
	public List<Holding> getHoldings()
	{
		return holdings;
	}
}
