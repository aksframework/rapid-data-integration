package com.lbg.rsk.cdp.demo.integration.crddec.entity;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DecisionResponseRepository extends CrudRepository<DecisionResponseEntity, Long> {}
