package vn.avq.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.avq.shop.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {}
