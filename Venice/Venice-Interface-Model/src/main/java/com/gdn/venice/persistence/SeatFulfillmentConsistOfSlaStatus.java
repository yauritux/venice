package com.gdn.venice.persistence;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


/**
 * The persistent class for the seat_fulfillment_consist_of_sla_status database table.
 * 
 */
@Entity
@Table(name="seat_fulfillment_consist_of_sla_status")
public class SeatFulfillmentConsistOfSlaStatus implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private SeatFulfillmentConsistOfSlaStatusPK id;
	
	//bi-directional one-to-one association to SeatOrderStatus
	@ManyToOne
	@JoinColumn(name="seat_order_status_id", nullable=false, insertable=false, updatable=false)
	private SeatOrderStatus seatOrderStatus;

	//bi-directional many-to-one association to SeatSlaStatus
    @ManyToOne
	@JoinColumn(name="sla_status_id", nullable=false, insertable=false, updatable=false)
	private SeatSlaStatus seatSlaStatus;

    public SeatFulfillmentConsistOfSlaStatus() {
    }

	public SeatFulfillmentConsistOfSlaStatusPK getId() {
		return this.id;
	}

	public void setId(SeatFulfillmentConsistOfSlaStatusPK id) {
		this.id = id;
	}
	
	public SeatOrderStatus getSeatOrderStatus() {
		return seatOrderStatus;
	}

	public void setSeatOrderStatus(SeatOrderStatus seatOrderStatus) {
		this.seatOrderStatus = seatOrderStatus;
	}

	public SeatSlaStatus getSeatSlaStatus() {
		return seatSlaStatus;
	}

	public void setSeatSlaStatus(SeatSlaStatus seatSlaStatus) {
		this.seatSlaStatus = seatSlaStatus;
	}
	
}