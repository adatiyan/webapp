package com.example.assignment1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.assignment1.model.Image;


@Repository
public interface ImageRepository extends JpaRepository<Image, Long>{

	@Query(value="SELECT * FROM IMAGE WHERE product_id =?",
			nativeQuery = true)
    List<Image> findImageByProductId(Long productId);
}
