package com.gdn.venice.persistence;

import java.io.Serializable;
import javax.persistence.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;


/**
 * The persistent class for the seat_order_status database table.
 * 
 */
@Entity
@Table(name="seat_order_status")
public class SeatOrderStatus implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="seat_order_status")  
	@TableGenerator(name="seat_order_status", table="openjpaseq", pkColumnName="id", valueColumnName="sequence_value", allocationSize=1)  //flush every 1 insert
	@Column(name="seat_order_status_id", unique=true, nullable=false)
	private Long seatOrderStatusId;

	@Column(name="by_user")
	private String byUser;

	@Column(name="order_status_decs")
	private String orderStatusDecs;

	//bi-directional many-to-one association to VenOrderStatus
    @ManyToOne
	@JoinColumn(name="order_status_id")
	private VenOrderStatus venOrderStatus;

	private String pic;

	@Column(name="update_date")
	private Timestamp updateDate;

	//bi-directional many-to-one association to SeatFulfillmentInPercentage
	@OneToMany(mappedBy="seatOrderStatus")
	private Set<SeatFulfillmentInPercentage> seatFulfillmentInPercentages;
	
	//bi-directional many-to-one association to SeatFulfillmentConsistOfSlaStatus
	@OneToMany(mappedBy="seatOrderStatus")
	private List<SeatFulfillmentConsistOfSlaStatus> seatFulfillmentConsistOfSlaStatus;

	//bi-directional many-to-many association to SeatSlaStatus
	@ManyToMany(mappedBy="seatOrderStatuses")
	private Set<SeatSlaStatus> seatSlaStatuses1;

	//bi-directional many-to-one association to SeatSlaStatus
	@OneToMany(mappedBy="seatOrderStatus")
	private Set<SeatSlaStatus> seatSlaStatuses;

    public SeatOrderStatus() {
    }

	public Long getSeatOrderStatusId() {
		return this.seatOrderStatusId;
	}

	public void setSeatOrderStatusId(Long seatOrderStatusId) {
		this.seatOrderStatusId = seatOrderStatusId;
	}

	public String getByUser() {
		return this.byUser;
	}

	public void setByUser(String byUser) {
		this.byUser = byUser;
	}

	public String getOrderStatusDecs() {
		return this.orderStatusDecs;
	}

	public void setOrderStatusDecs(String orderStatusDecs) {
		this.orderStatusDecs = orderStatusDecs;
	}

	public String getPic() {
		return this.pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}

	public Timestamp getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(Timestamp updateDate) {
		this.updateDate = updateDate;
	}

	public Set<SeatFulfillmentInPercentage> getSeatFulfillmentInPercentages() {
		return this.seatFulfillmentInPercentages;
	}

	public void setSeatFulfillmentInPercentages(Set<SeatFulfillmentInPercentage> seatFulfillmentInPercentages) {
		this.seatFulfillmentInPercentages = seatFulfillmentInPercentages;
	}
	
	public Set<SeatSlaStatus> getSeatSlaStatuses1() {
		return this.seatSlaStatuses1;
	}

	public void setSeatSlaStatuses1(Set<SeatSlaStatus> seatSlaStatuses1) {
		this.seatSlaStatuses1 = seatSlaStatuses1;
	}
	
	public Set<SeatSlaStatus> getSeatSlaStatuses() {
		return this.seatSlaStatuses;
	}

	public void setSeatSlaStatuses(Set<SeatSlaStatus> seatSlaStatuses) {
		this.seatSlaStatuses = seatSlaStatuses;
	}
	
	public List<SeatFulfillmentConsistOfSlaStatus> getSeatFulfillmentConsistOfSlaStatus() {
		return seatFulfillmentConsistOfSlaStatus;
	}

	public void setSeatFulfillmentConsistOfSlaStatus(
			List<SeatFulfillmentConsistOfSlaStatus> seatFulfillmentConsistOfSlaStatus) {
		this.seatFulfillmentConsistOfSlaStatus = seatFulfillmentConsistOfSlaStatus;
	}	

	public VenOrderStatus getVenOrderStatus() {
		return venOrderStatus;
	}

	public void setVenOrderStatus(VenOrderStatus venOrderStatus) {
		this.venOrderStatus = venOrderStatus;
	}


}