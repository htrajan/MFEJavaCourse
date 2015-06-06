package edu.berkeley.exchange.order;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.berkeley.exchange.order.Order.OrderType;
import edu.berkeley.exchange.security.Security;

public interface OrderRepository extends JpaRepository<Order, Long> {
	
	public Order findTopBySecurityAndTypeOrderByPriceDesc(Security security, OrderType type);
	public Order findTopBySecurityAndTypeOrderByPriceAsc(Security security, OrderType type);
}
