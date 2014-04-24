package com.gdn.venice.persistence;

import java.io.Serializable;
import javax.persistence.*;

import java.sql.Timestamp;
import java.math.BigDecimal;
import java.util.Set;


/**
 * The persistent class for the seat_fulfillment_in_percentage database table.
 * 
 */
@Entity
@Table(name="seat_fulfillment_in_percentage")
public class SeatFulfillmentInPercentage implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="seat_fulfillment_in_percentage")  
	@TableGenerator(name="seat_fulfillment_in_percentage", table="openjpaseq", pkColumnName="id", valueColumnName="sequence_value", allocationSize=1)  //flush every 1 insert
	@Column(name="fulfillment_in_percentage_id", unique=true, nullable=false)	
	private Long fulfillmentInPercentageId;

	@Column(name="by_user")
	private String byUser;
	
	@Column(precision=20, scale=2)
	private BigDecimal max;
	
	@Column(precision=20, scale=2)
	private BigDecimal min;

	@Column(name="update_date")
	private Timestamp updateDate;

	//bi-directional many-to-one association to SeatOrderStatus
    @ManyToOne
	@JoinColumn(name="seat_order_status_id")
	private SeatOrderStatus seatOrderStatus;

	//bi-directional many-to-one association to SeatResultStatusTracking
    @ManyToOne
	@JoinColumn(name="result_status_tracking_id")
	private SeatResultStatusTracking seatResultStatusTracking;

	//bi-directional many-to-one association to SeatOrderFulfillment
	@OneToMany(mappedBy="seatFulfillmentInPercentage")
	private Set<SeatOrderFulfillment> seatOrderFulfillments;

    public SeatFulfillmentInPercentage() {
    }

	public Long getFulfillmentInPercentageId() {
		return this.fulfillmentInPercentageId;
	}

	public void setFulfillmentInPercentageId(Long fulfillmentInPercentageId) {
		this.fulfillmentInPercentageId = fulfillmentInPercentageId;
	}

	public String getByUser() {
		return this.byUser;
	}

	public void setByUser(String byUser) {
		this.byUser = byUser;
	}

	public BigDecimal getMax() {
		return this.max;
	}

	public void setMax(BigDecimal max) {
		this.max = max;
	}

	public BigDecimal getMin() {
		return this.min;
	}

	public void setMin(BigDecimal min) {
		this.min = min;
	}

	public Timestamp getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(Timestamp updateDate) {
		this.updateDate = updateDate;
	}

	public SeatOrderStatus getSeatOrderStatus() {
		return this.seatOrderStatus;
	}

	public void setSeatOrderStatus(SeatOrderStatus seatOrderStatus) {
		this.seatOrderStatus = seatOrderStatus;
	}
	
	public SeatResultStatusTracking getSeatResultStatusTracking() {
		return this.seatResultStatusTracking;
	}

	public void setSeatResultStatusTracking(SeatResultStatusTracking seatResultStatusTracking) {
		this.seatResultStatusTracking = seatResultStatusTracking;
	}
	
	public Set<SeatOrderFulfillment> getSeatOrderFulfillments() {
		return this.seatOrderFulfillments;
	}

	public void setSeatOrderFulfillments(Set<SeatOrderFulfillment> seatOrderFulfillments) {
		this.seatOrderFulfillments = seatOrderFulfillments;
	}
	
}