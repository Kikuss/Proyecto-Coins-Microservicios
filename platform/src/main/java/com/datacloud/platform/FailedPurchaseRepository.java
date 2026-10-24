package com.datacloud.platform;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface FailedPurchaseRepository extends JpaRepository<FailedPurchase,Long> {
 List<FailedPurchase> findByIdUserOrderByIdDesc(Integer idUser);
}
