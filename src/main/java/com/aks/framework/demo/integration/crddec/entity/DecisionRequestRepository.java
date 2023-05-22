package com.aks.framework.demo.integration.crddec.entity;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DecisionRequestRepository extends CrudRepository<DecisionRequestEntity, Long> {}
