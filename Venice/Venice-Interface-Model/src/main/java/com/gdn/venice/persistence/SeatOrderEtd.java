package com.gdn.venice.persistence;

import java.io.Serializable;
import javax.persistence.*;

import java.sql.Timestamp;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;


/**
 * The persistent class for the seat_order_etd database table.
 * 
 */
@Entity
@Table(name="seat_order_etd")
public class SeatOrderEtd implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="seat_order_etd")  
	@TableGenerator(name="seat_order_etd", table="openjpaseq", pkColumnName="id", valueColumnName="sequence_value", allocationSize=1)  //flush every 1 insert
	@Column(name="order_etd_id", unique=true, nullable=false)
	private Long orderEtdId;

	@Column(name="by_user")
	private String byUser;
	
	@Column(name="wcs_order_id")
	private String wcsOrderId;
	
	@Column(name="diff_etd",precision=20, scale=2)
	private BigDecimal diffEtd;

    @Temporal( TemporalType.DATE)
	@Column(name="etd_max")
	private Date etdMax;

    @Temporal( TemporalType.DATE)
	@Column(name="etd_min")
	private Date etdMin;

	private String other;

	private String reason;

	@Column(name="update_etd_date")
	private Timestamp updateEtdDate;

	//bi-directional many-to-one association to SeatOrderStatusHistory
	@OneToMany(mappedBy="seatOrderEtd")
	private Set<SeatOrderStatusHistory> seatOrderStatusHistories;

    public SeatOrderEtd() {
    }

	public Long getOrderEtdId() {
		return this.orderEtdId;
	}

	public void setOrderEtdId(Long orderEtdId) {
		this.orderEtdId = orderEtdId;
	}

	public String getByUser() {
		return this.byUser;
	}

	public void setByUser(String byUser) {
		this.byUser = byUser;
	}

	public BigDecimal getDiffEtd() {
		return this.diffEtd;
	}

	public void setDiffEtd(BigDecimal diffEtd) {
		this.diffEtd = diffEtd;
	}

	public Date getEtdMax() {
		return this.etdMax;
	}

	public void setEtdMax(Date etdMax) {
		this.etdMax = etdMax;
	}

	public Date getEtdMin() {
		return this.etdMin;
	}

	public void setEtdMin(Date etdMin) {
		this.etdMin = etdMin;
	}

	public String getOther() {
		return this.other;
	}

	public void setOther(String other) {
		this.other = other;
	}

	public String getReason() {
		return this.reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Timestamp getUpdateEtdDate() {
		return this.updateEtdDate;
	}

	public void setUpdateEtdDate(Timestamp updateEtdDate) {
		this.updateEtdDate = updateEtdDate;
	}

	public Set<SeatOrderStatusHistory> getSeatOrderStatusHistories() {
		return this.seatOrderStatusHistories;
	}

	public void setSeatOrderStatusHistories(Set<SeatOrderStatusHistory> seatOrderStatusHistories) {
		this.seatOrderStatusHistories = seatOrderStatusHistories;
	}
	
	public String getWcsOrderId() {
		return wcsOrderId;
	}

	public void setWcsOrderId(String wcsOrderId) {
		this.wcsOrderId = wcsOrderId;
	}
	
}