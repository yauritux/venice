package com.gdn.venice.persistence;

import java.io.Serializable;
import javax.persistence.*;

import java.sql.Timestamp;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;


/**
 * The persistent class for the seat_sla_status database table.
 * 
 */
@Entity
@Table(name="seat_sla_status")
public class SeatSlaStatus implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="seat_sla_status")  
	@TableGenerator(name="seat_sla_status", table="openjpaseq", pkColumnName="id", valueColumnName="sequence_value", allocationSize=1)  //flush every 1 insert
	@Column(name="sla_status_id", unique=true, nullable=false)
	private Long slaStatusId;

	@Column(name="by_user")
	private String byUser;

	private BigDecimal sla;

	@Column(name="sla_second")
	private BigDecimal slaSecond;

	@Column(name="update_date")
	private Timestamp updateDate;

	//bi-directional many-to-many association to SeatOrderStatus
    @ManyToMany
	@JoinTable(
		name="seat_fulfillment_consist_of_sla_status"
		, joinColumns={
			@JoinColumn(name="sla_status_id")
			}
		, inverseJoinColumns={
			@JoinColumn(name="seat_order_status_id")
			}
		)
	private Set<SeatOrderStatus> seatOrderStatuses;

	//bi-directional many-to-one association to SeatOrderStatus
    @ManyToOne
	@JoinColumn(name="seat_order_status_id")
	private SeatOrderStatus seatOrderStatus;

	//bi-directional many-to-one association to SeatStatusUom
    @ManyToOne
	@JoinColumn(name="status_uom_id")
	private SeatStatusUom seatStatusUom;

	//bi-directional many-to-one association to SeatSlaStatusPercentage
	@OneToMany(mappedBy="seatSlaStatus")
	private Set<SeatSlaStatusPercentage> seatSlaStatusPercentages;
	
	//bi-directional many-to-one association to SeatFulfillmentConsistOfSlaStatus
	@OneToMany(mappedBy="seatSlaStatus")
	private List<SeatFulfillmentConsistOfSlaStatus> seatFulfillmentConsistOfSlaStatus;

    public SeatSlaStatus() {
    }

	public Long getSlaStatusId() {
		return this.slaStatusId;
	}

	public void setSlaStatusId(Long slaStatusId) {
		this.slaStatusId = slaStatusId;
	}

	public String getByUser() {
		return this.byUser;
	}

	public void setByUser(String byUser) {
		this.byUser = byUser;
	}

	public BigDecimal getSla() {
		return this.sla;
	}

	public void setSla(BigDecimal sla) {
		this.sla = sla;
	}

	public BigDecimal getSlaSecond() {
		return this.slaSecond;
	}

	public void setSlaSecond(BigDecimal slaSecond) {
		this.slaSecond = slaSecond;
	}

	public Timestamp getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(Timestamp updateDate) {
		this.updateDate = updateDate;
	}

	public Set<SeatOrderStatus> getSeatOrderStatuses() {
		return this.seatOrderStatuses;
	}

	public void setSeatOrderStatuses(Set<SeatOrderStatus> seatOrderStatuses) {
		this.seatOrderStatuses = seatOrderStatuses;
	}
	
	public SeatOrderStatus getSeatOrderStatus() {
		return this.seatOrderStatus;
	}

	public void setSeatOrderStatus(SeatOrderStatus seatOrderStatus) {
		this.seatOrderStatus = seatOrderStatus;
	}
	
	public SeatStatusUom getSeatStatusUom() {
		return this.seatStatusUom;
	}

	public void setSeatStatusUom(SeatStatusUom seatStatusUom) {
		this.seatStatusUom = seatStatusUom;
	}
	
	public Set<SeatSlaStatusPercentage> getSeatSlaStatusPercentages() {
		return this.seatSlaStatusPercentages;
	}

	public void setSeatSlaStatusPercentages(Set<SeatSlaStatusPercentage> seatSlaStatusPercentages) {
		this.seatSlaStatusPercentages = seatSlaStatusPercentages;
	}
	
	public List<SeatFulfillmentConsistOfSlaStatus> getSeatFulfillmentConsistOfSlaStatus() {
		return seatFulfillmentConsistOfSlaStatus;
	}

	public void setSeatFulfillmentConsistOfSlaStatus(
			List<SeatFulfillmentConsistOfSlaStatus> seatFulfillmentConsistOfSlaStatus) {
		this.seatFulfillmentConsistOfSlaStatus = seatFulfillmentConsistOfSlaStatus;
	}
	
}