package com.gdn.venice.persistence;

import java.io.Serializable;
import javax.persistence.*;

import java.sql.Timestamp;
import java.math.BigDecimal;


/**
 * The persistent class for the seat_order_fulfillment database table.
 * 
 */
@Entity
@Table(name="seat_order_fulfillment")
public class SeatOrderFulfillment implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="seat_order_fulfillment")  
	@TableGenerator(name="seat_order_fulfillment", table="openjpaseq", pkColumnName="id", valueColumnName="sequence_value", allocationSize=1)  //flush every 1 insert
	@Column(name="order_fulfillment_id", unique=true, nullable=false)
	private Long orderFulfillmentId;

	@Column(name="created_date")
	private Timestamp createdDate;

	@Column(name="etd_order_complete")
	private Timestamp etdOrderComplete;

	@Column(name="issue_id")
	private String issueId;

	@Column(name="new_etd_max_order")
	private Timestamp newEtdMaxOrder;

	@Column(name="order_process_late_time")
	private String orderProcessLateTime;

	@Column(name="order_process_late_time_second")
	private BigDecimal orderProcessLateTimeSecond;

	@Column(name="order_status_dec")
	private String orderStatusDec;

	@Column(name="status_issue")
	private Boolean statusIssue;

	//bi-directional many-to-one association to SeatFulfillmentInPercentage
    @ManyToOne
	@JoinColumn(name="fulfillment_in_percentage_id")
	private SeatFulfillmentInPercentage seatFulfillmentInPercentage;

	//bi-directional many-to-one association to SeatOrderStatusHistory
    @ManyToOne
	@JoinColumn(name="seat_order_status_history_id")
	private SeatOrderStatusHistory seatOrderStatusHistory;

    public SeatOrderFulfillment() {
    }

	public Long getOrderFulfillmentId() {
		return this.orderFulfillmentId;
	}

	public void setOrderFulfillmentId(Long orderFulfillmentId) {
		this.orderFulfillmentId = orderFulfillmentId;
	}

	public Timestamp getCreatedDate() {
		return this.createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public Timestamp getEtdOrderComplete() {
		return this.etdOrderComplete;
	}

	public void setEtdOrderComplete(Timestamp etdOrderComplete) {
		this.etdOrderComplete = etdOrderComplete;
	}

	public String getIssueId() {
		return this.issueId;
	}

	public void setIssueId(String issueId) {
		this.issueId = issueId;
	}

	public Timestamp getNewEtdMaxOrder() {
		return this.newEtdMaxOrder;
	}

	public void setNewEtdMaxOrder(Timestamp newEtdMaxOrder) {
		this.newEtdMaxOrder = newEtdMaxOrder;
	}

	public String getOrderProcessLateTime() {
		return this.orderProcessLateTime;
	}

	public void setOrderProcessLateTime(String orderProcessLateTime) {
		this.orderProcessLateTime = orderProcessLateTime;
	}

	public BigDecimal getOrderProcessLateTimeSecond() {
		return this.orderProcessLateTimeSecond;
	}

	public void setOrderProcessLateTimeSecond(BigDecimal orderProcessLateTimeSecond) {
		this.orderProcessLateTimeSecond = orderProcessLateTimeSecond;
	}

	public String getOrderStatusDec() {
		return this.orderStatusDec;
	}

	public void setOrderStatusDec(String orderStatusDec) {
		this.orderStatusDec = orderStatusDec;
	}

	public Boolean getStatusIssue() {
		return this.statusIssue;
	}

	public void setStatusIssue(Boolean statusIssue) {
		this.statusIssue = statusIssue;
	}

	public SeatFulfillmentInPercentage getSeatFulfillmentInPercentage() {
		return this.seatFulfillmentInPercentage;
	}

	public void setSeatFulfillmentInPercentage(SeatFulfillmentInPercentage seatFulfillmentInPercentage) {
		this.seatFulfillmentInPercentage = seatFulfillmentInPercentage;
	}
	
	public SeatOrderStatusHistory getSeatOrderStatusHistory() {
		return this.seatOrderStatusHistory;
	}

	public void setSeatOrderStatusHistory(SeatOrderStatusHistory seatOrderStatusHistory) {
		this.seatOrderStatusHistory = seatOrderStatusHistory;
	}
	
}