package com.gdn.venice.persistence;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the seat_fulfillment_consist_of_sla_status database table.
 * 
 */
@Embeddable
public class SeatFulfillmentConsistOfSlaStatusPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="sla_status_id")
	private Long slaStatusId;

	@Column(name="seat_order_status_id")
	private Long seatOrderStatusId;

    public SeatFulfillmentConsistOfSlaStatusPK() {
    }
	public Long getSlaStatusId() {
		return this.slaStatusId;
	}
	public void setSlaStatusId(Long slaStatusId) {
		this.slaStatusId = slaStatusId;
	}
	public Long getSeatOrderStatusId() {
		return this.seatOrderStatusId;
	}
	public void setSeatOrderStatusId(Long seatOrderStatusId) {
		this.seatOrderStatusId = seatOrderStatusId;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SeatFulfillmentConsistOfSlaStatusPK)) {
			return false;
		}
		SeatFulfillmentConsistOfSlaStatusPK castOther = (SeatFulfillmentConsistOfSlaStatusPK)other;
		return 
			this.slaStatusId.equals(castOther.slaStatusId)
			&& this.seatOrderStatusId.equals(castOther.seatOrderStatusId);

    }
    
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.slaStatusId.hashCode();
		hash = hash * prime + this.seatOrderStatusId.hashCode();
		
		return hash;
    }
}