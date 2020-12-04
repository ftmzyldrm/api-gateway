package com.example.apigateway.repo;

import com.example.apigateway.model.Path;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PathRepository extends CrudRepository<Path,String> {
}
