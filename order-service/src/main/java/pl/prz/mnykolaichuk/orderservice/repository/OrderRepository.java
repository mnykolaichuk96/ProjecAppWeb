package pl.prz.mnykolaichuk.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.prz.mnykolaichuk.orderservice.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
