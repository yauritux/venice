package com.gdn.venice.persistence;

import java.io.Serializable;
import javax.persistence.*;

import java.util.Set;


/**
 * The persistent class for the seat_result_status_tracking database table.
 * 
 */
@Entity
@Table(name="seat_result_status_tracking")
public class SeatResultStatusTracking implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="seat_result_status_tracking")  
	@TableGenerator(name="seat_result_status_tracking", table="openjpaseq", pkColumnName="id", valueColumnName="sequence_value", allocationSize=1)  //flush every 1 insert
	@Column(name="result_status_tracking_id", unique=true, nullable=false)
	private Long resultStatusTrackingId;

	@Column(name="result_status_tracking_desc")
	private String resultStatusTrackingDesc;

	//bi-directional many-to-one association to SeatFulfillmentInPercentage
	@OneToMany(mappedBy="seatResultStatusTracking")
	private Set<SeatFulfillmentInPercentage> seatFulfillmentInPercentages;

	//bi-directional many-to-one association to SeatSlaStatusPercentage
	@OneToMany(mappedBy="seatResultStatusTracking")
	private Set<SeatSlaStatusPercentage> seatSlaStatusPercentages;

    public SeatResultStatusTracking() {
    }

	public Long getResultStatusTrackingId() {
		return this.resultStatusTrackingId;
	}

	public void setResultStatusTrackingId(Long resultStatusTrackingId) {
		this.resultStatusTrackingId = resultStatusTrackingId;
	}

	public String getResultStatusTrackingDesc() {
		return this.resultStatusTrackingDesc;
	}

	public void setResultStatusTrackingDesc(String resultStatusTrackingDesc) {
		this.resultStatusTrackingDesc = resultStatusTrackingDesc;
	}

	public Set<SeatFulfillmentInPercentage> getSeatFulfillmentInPercentages() {
		return this.seatFulfillmentInPercentages;
	}

	public void setSeatFulfillmentInPercentages(Set<SeatFulfillmentInPercentage> seatFulfillmentInPercentages) {
		this.seatFulfillmentInPercentages = seatFulfillmentInPercentages;
	}
	
	public Set<SeatSlaStatusPercentage> getSeatSlaStatusPercentages() {
		return this.seatSlaStatusPercentages;
	}

	public void setSeatSlaStatusPercentages(Set<SeatSlaStatusPercentage> seatSlaStatusPercentages) {
		this.seatSlaStatusPercentages = seatSlaStatusPercentages;
	}
	
}