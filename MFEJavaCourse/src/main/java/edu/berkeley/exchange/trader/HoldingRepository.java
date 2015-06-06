package edu.berkeley.exchange.trader;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, HoldingKey> {

}
