package edu.berkeley.exchange;

public class OrderExecutionException extends Exception {

	private static final long serialVersionUID = 3434736956934144356L;
	
	public OrderExecutionException(String message)
	{
		super(message);
	}
}
