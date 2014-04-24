package com.gdn.venice.persistence;

import java.io.Serializable;
import javax.persistence.*;

import java.sql.Timestamp;
import java.math.BigDecimal;


/**
 * The persistent class for the seat_order_status_tracking database table.
 * 
 */
@Entity
@Table(name="seat_order_status_tracking")
public class SeatOrderStatusTracking implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="seat_order_status_tracking")  
	@TableGenerator(name="seat_order_status_tracking", table="openjpaseq", pkColumnName="id", valueColumnName="sequence_value", allocationSize=1)  //flush every 1 insert
	@Column(name="seat_order_status_tracking_id", unique=true, nullable=false)
	private Long seatOrderStatusTrackingId;

	@Column(name="created_date")
	private Timestamp createdDate;

	@Column(name="issue_id")
	private String issueId;

	@Column(name="status_desc")
	private String statusDesc;

	@Column(name="status_due_date")
	private Timestamp statusDueDate;

	@Column(name="status_issue")
	private Boolean statusIssue;

	@Column(name="status_late_time")
	private String statusLateTime;

	@Column(name="status_late_time_second")
	private BigDecimal statusLateTimeSecond;

	@Column(name="status_timestamp")
	private Timestamp statusTimestamp;

	//bi-directional many-to-one association to SeatSlaStatusPercentage
    @ManyToOne
	@JoinColumn(name="seat_sla_status_percentage_id")
	private SeatSlaStatusPercentage seatSlaStatusPercentage;

	//bi-directional many-to-one association to SeatOrderStatusHistory
    @ManyToOne
	@JoinColumn(name="seat_order_status_history_id")
	private SeatOrderStatusHistory seatOrderStatusHistory;

    public SeatOrderStatusTracking() {
    }

	public Long getSeatOrderStatusTrackingId() {
		return this.seatOrderStatusTrackingId;
	}

	public void setSeatOrderStatusTrackingId(Long seatOrderStatusTrackingId) {
		this.seatOrderStatusTrackingId = seatOrderStatusTrackingId;
	}

	public Timestamp getCreatedDate() {
		return this.createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public String getIssueId() {
		return this.issueId;
	}

	public void setIssueId(String issueId) {
		this.issueId = issueId;
	}

	public String getStatusDesc() {
		return this.statusDesc;
	}

	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}

	public Timestamp getStatusDueDate() {
		return this.statusDueDate;
	}

	public void setStatusDueDate(Timestamp statusDueDate) {
		this.statusDueDate = statusDueDate;
	}

	public Boolean getStatusIssue() {
		return this.statusIssue;
	}

	public void setStatusIssue(Boolean statusIssue) {
		this.statusIssue = statusIssue;
	}

	public String getStatusLateTime() {
		return this.statusLateTime;
	}

	public void setStatusLateTime(String statusLateTime) {
		this.statusLateTime = statusLateTime;
	}

	public BigDecimal getStatusLateTimeSecond() {
		return this.statusLateTimeSecond;
	}

	public void setStatusLateTimeSecond(BigDecimal statusLateTimeSecond) {
		this.statusLateTimeSecond = statusLateTimeSecond;
	}

	public Timestamp getStatusTimestamp() {
		return this.statusTimestamp;
	}

	public void setStatusTimestamp(Timestamp statusTimestamp) {
		this.statusTimestamp = statusTimestamp;
	}

	public SeatSlaStatusPercentage getSeatSlaStatusPercentage() {
		return this.seatSlaStatusPercentage;
	}

	public void setSeatSlaStatusPercentage(SeatSlaStatusPercentage seatSlaStatusPercentage) {
		this.seatSlaStatusPercentage = seatSlaStatusPercentage;
	}
	
	public SeatOrderStatusHistory getSeatOrderStatusHistory() {
		return this.seatOrderStatusHistory;
	}

	public void setSeatOrderStatusHistory(SeatOrderStatusHistory seatOrderStatusHistory) {
		this.seatOrderStatusHistory = seatOrderStatusHistory;
	}
	
}