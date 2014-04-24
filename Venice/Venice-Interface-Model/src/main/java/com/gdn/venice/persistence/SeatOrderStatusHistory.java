package com.gdn.venice.persistence;

import java.io.Serializable;
import javax.persistence.*;

import java.sql.Timestamp;
import java.util.Set;


/**
 * The persistent class for the seat_order_status_history database table.
 * 
 */
@Entity
@Table(name="seat_order_status_history")
public class SeatOrderStatusHistory implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="seat_order_status_history")  
	@TableGenerator(name="seat_order_status_history", table="openjpaseq", pkColumnName="id", valueColumnName="sequence_value", allocationSize=1)  //flush every 1 insert
	@Column(name="seat_order_status_history_id", unique=true, nullable=false)
	private Long seatOrderStatusHistoryId;

	//bi-directional many-to-one association to SeatOrderStatus
    @ManyToOne
	@JoinColumn(name="order_id",nullable=false)
	private VenOrder venOrder;    
	
   	@ManyToOne
	@JoinColumn(name="order_item_id",nullable=false)
	private VenOrderItem venOrderItem;    
    
    @ManyToOne
	@JoinColumn(name="order_status_id",nullable=false)
	private VenOrderStatus venOrderStatus;    
    
    @Column(name="update_status_date")
	private Timestamp updateStatusDate;

	//bi-directional many-to-one association to SeatOrderEtd
    @ManyToOne
	@JoinColumn(name="order_etd_id")
	private SeatOrderEtd seatOrderEtd;

	//bi-directional many-to-one association to SeatOrderFulfillment
	@OneToMany(mappedBy="seatOrderStatusHistory")
	private Set<SeatOrderFulfillment> seatOrderFulfillments;

	//bi-directional many-to-one association to SeatOrderStatusTracking
	@OneToMany(mappedBy="seatOrderStatusHistory")
	private Set<SeatOrderStatusTracking> seatOrderStatusTrackings;

    public SeatOrderStatusHistory() {
    }

	public Long getSeatOrderStatusHistoryId() {
		return this.seatOrderStatusHistoryId;
	}

	public void setSeatOrderStatusHistoryId(Long seatOrderStatusHistoryId) {
		this.seatOrderStatusHistoryId = seatOrderStatusHistoryId;
	}

	public Timestamp getUpdateStatusDate() {
		return this.updateStatusDate;
	}

	public void setUpdateStatusDate(Timestamp updateStatusDate) {
		this.updateStatusDate = updateStatusDate;
	}

	public SeatOrderEtd getSeatOrderEtd() {
		return this.seatOrderEtd;
	}

	public void setSeatOrderEtd(SeatOrderEtd seatOrderEtd) {
		this.seatOrderEtd = seatOrderEtd;
	}
	
	public Set<SeatOrderFulfillment> getSeatOrderFulfillments() {
		return this.seatOrderFulfillments;
	}

	public void setSeatOrderFulfillments(Set<SeatOrderFulfillment> seatOrderFulfillments) {
		this.seatOrderFulfillments = seatOrderFulfillments;
	}
	
	public Set<SeatOrderStatusTracking> getSeatOrderStatusTrackings() {
		return this.seatOrderStatusTrackings;
	}

	public void setSeatOrderStatusTrackings(Set<SeatOrderStatusTracking> seatOrderStatusTrackings) {
		this.seatOrderStatusTrackings = seatOrderStatusTrackings;
	}
	 public VenOrder getVenOrder() {
			return venOrder;
		}

		public void setVenOrder(VenOrder venOrder) {
			this.venOrder = venOrder;
		}

		public VenOrderItem getVenOrderItem() {
			return venOrderItem;
		}

		public void setVenOrderItem(VenOrderItem venOrderItem) {
			this.venOrderItem = venOrderItem;
		}

		public VenOrderStatus getVenOrderStatus() {
			return venOrderStatus;
		}

		public void setVenOrderStatus(VenOrderStatus venOrderStatus) {
			this.venOrderStatus = venOrderStatus;
		}
}