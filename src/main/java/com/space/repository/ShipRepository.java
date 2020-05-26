package com.space.repository;

import com.space.model.Ship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;


public interface ShipRepository extends PagingAndSortingRepository<Ship, Long>, JpaSpecificationExecutor {

    @Override
    Page<Ship> findAll(Specification spec, Pageable pageable);

    @Override
    List<Ship> findAll(Specification spec);
}
