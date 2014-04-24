package com.gdn.venice.persistence;

import java.io.Serializable;
import javax.persistence.*;

import java.math.BigDecimal;
import java.util.Set;


/**
 * The persistent class for the seat_sla_status_percentage database table.
 * 
 */
@Entity
@Table(name="seat_sla_status_percentage")
public class SeatSlaStatusPercentage implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="seat_sla_status_percentage")  
	@TableGenerator(name="seat_sla_status_percentage", table="openjpaseq", pkColumnName="id", valueColumnName="sequence_value", allocationSize=1)  //flush every 1 insert
	@Column(name="seat_sla_status_percentage_id", unique=true, nullable=false)
	private Long seatSlaStatusPercentageId;

	private BigDecimal max;

	private BigDecimal min;

	//bi-directional many-to-one association to SeatOrderStatusTracking
	@OneToMany(mappedBy="seatSlaStatusPercentage")
	private Set<SeatOrderStatusTracking> seatOrderStatusTrackings;

	//bi-directional many-to-one association to SeatResultStatusTracking
    @ManyToOne
	@JoinColumn(name="result_status_tracking_id")
	private SeatResultStatusTracking seatResultStatusTracking;

	//bi-directional many-to-one association to SeatSlaStatus
    @ManyToOne
	@JoinColumn(name="sla_status_id")
	private SeatSlaStatus seatSlaStatus;

    public SeatSlaStatusPercentage() {
    }

	public Long getSeatSlaStatusPercentageId() {
		return this.seatSlaStatusPercentageId;
	}

	public void setSeatSlaStatusPercentageId(Long seatSlaStatusPercentageId) {
		this.seatSlaStatusPercentageId = seatSlaStatusPercentageId;
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

	public Set<SeatOrderStatusTracking> getSeatOrderStatusTrackings() {
		return this.seatOrderStatusTrackings;
	}

	public void setSeatOrderStatusTrackings(Set<SeatOrderStatusTracking> seatOrderStatusTrackings) {
		this.seatOrderStatusTrackings = seatOrderStatusTrackings;
	}
	
	public SeatResultStatusTracking getSeatResultStatusTracking() {
		return this.seatResultStatusTracking;
	}

	public void setSeatResultStatusTracking(SeatResultStatusTracking seatResultStatusTracking) {
		this.seatResultStatusTracking = seatResultStatusTracking;
	}
	
	public SeatSlaStatus getSeatSlaStatus() {
		return this.seatSlaStatus;
	}

	public void setSeatSlaStatus(SeatSlaStatus seatSlaStatus) {
		this.seatSlaStatus = seatSlaStatus;
	}
	
}