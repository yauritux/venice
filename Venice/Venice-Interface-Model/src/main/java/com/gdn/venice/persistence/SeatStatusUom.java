package com.gdn.venice.persistence;

import java.io.Serializable;
import javax.persistence.*;

import java.sql.Timestamp;
import java.math.BigDecimal;
import java.util.Set;


/**
 * The persistent class for the seat_status_uom database table.
 * 
 */
@Entity
@Table(name="seat_status_uom")
public class SeatStatusUom implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="seat_status_uom")  
	@TableGenerator(name="seat_status_uom", table="openjpaseq", pkColumnName="id", valueColumnName="sequence_value", allocationSize=1)  //flush every 1 insert
	@Column(name="status_uom_id", unique=true, nullable=false)
	private Long statusUomId;

	@Column(name="by_user")
	private String byUser;

	@Column(name="status_uom_desc")
	private String statusUomDesc;

	@Column(name="status_uom_end")
	private BigDecimal statusUomEnd;

	@Column(name="status_uom_from")
	private BigDecimal statusUomFrom;

	@Column(name="status_uom_type")
	private String statusUomType;

	@Column(name="update_date")
	private Timestamp updateDate;

	//bi-directional many-to-one association to SeatSlaStatus
	@OneToMany(mappedBy="seatStatusUom")
	private Set<SeatSlaStatus> seatSlaStatuses;

    public SeatStatusUom() {
    }

	public Long getStatusUomId() {
		return this.statusUomId;
	}

	public void setStatusUomId(Long statusUomId) {
		this.statusUomId = statusUomId;
	}

	public String getByUser() {
		return this.byUser;
	}

	public void setByUser(String byUser) {
		this.byUser = byUser;
	}

	public String getStatusUomDesc() {
		return this.statusUomDesc;
	}

	public void setStatusUomDesc(String statusUomDesc) {
		this.statusUomDesc = statusUomDesc;
	}

	public BigDecimal getStatusUomEnd() {
		return this.statusUomEnd;
	}

	public void setStatusUomEnd(BigDecimal statusUomEnd) {
		this.statusUomEnd = statusUomEnd;
	}

	public BigDecimal getStatusUomFrom() {
		return this.statusUomFrom;
	}

	public void setStatusUomFrom(BigDecimal statusUomFrom) {
		this.statusUomFrom = statusUomFrom;
	}

	public String getStatusUomType() {
		return this.statusUomType;
	}

	public void setStatusUomType(String statusUomType) {
		this.statusUomType = statusUomType;
	}

	public Timestamp getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(Timestamp updateDate) {
		this.updateDate = updateDate;
	}

	public Set<SeatSlaStatus> getSeatSlaStatuses() {
		return this.seatSlaStatuses;
	}

	public void setSeatSlaStatuses(Set<SeatSlaStatus> seatSlaStatuses) {
		this.seatSlaStatuses = seatSlaStatuses;
	}
	
}