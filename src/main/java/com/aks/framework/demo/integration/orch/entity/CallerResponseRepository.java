package com.aks.framework.demo.integration.orch.entity;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CallerResponseRepository extends CrudRepository<CallerResponseEntity, Long> {}
