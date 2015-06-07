package edu.berkeley.exchange.trader;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Embeddable;

@Embeddable
public class HoldingKey implements Serializable
{
	private static final long serialVersionUID = -3562898376349687851L;
	
	@Basic
	private String traderName;
	
	@Basic
	private String ticker;
	
	protected HoldingKey()
	{
		
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ticker == null) ? 0 : ticker.hashCode());
		result = prime * result
				+ ((traderName == null) ? 0 : traderName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HoldingKey other = (HoldingKey) obj;
		if (ticker == null) {
			if (other.ticker != null)
				return false;
		} else if (!ticker.equals(other.ticker))
			return false;
		if (traderName == null) {
			if (other.traderName != null)
				return false;
		} else if (!traderName.equals(other.traderName))
			return false;
		return true;
	}

	public HoldingKey(String traderName, String ticker) 
	{
		this.setTraderName(traderName);
		this.setTicker(ticker);
	}

	public String getTraderName() {
		return traderName;
	}

	public void setTraderName(String traderName) {
		this.traderName = traderName;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
}