package com.lbg.rsk.cdp.demo.integration.orch.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.ALWAYS)
@Table(name = "COMM_REQUEST")
public class CallerRequestEntity {

  @Column(name = "CrdRqsId")
  @Id
  private String crdRqsId;

  @Column(name = "ApplicationCustomerId")
  private String applicationCustomerId;

  @Column(name = "CustomerSegmentType")
  private String customerSegmentType;

  @Column(name = "StrategyType")
  private String strategyType;

  @Column(name = "DbRequestType")
  private String dbRequestType;

  @Column(name = "ChannelType")
  private String channelType;

  @Column(name = "CreatedAt", updatable = false)
  @CreationTimestamp
  private OffsetDateTime createdAt;

  @Column(name = "Json", columnDefinition = "json")
  private String payload;
}
