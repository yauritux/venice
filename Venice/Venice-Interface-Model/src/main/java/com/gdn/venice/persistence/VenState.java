package com.gdn.venice.persistence;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;


/**
 * The persistent class for the ven_state database table.
 * 
 * Change History:
 *  March 27, 2014 (1:59PM) - yauritux : 
 *    - override equals and hashCode methods 
 *    - removes bidirectional association (one-to-many)
 */
@Entity
@Table(name="ven_state")
public class VenState implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
@GeneratedValue(strategy=GenerationType.TABLE, generator="ven_state")  
	@TableGenerator(name="ven_state", table="openjpaseq", pkColumnName="id", valueColumnName="sequence_value", allocationSize=1)  //flush every 1 insert
	@Column(name="state_id", unique=true, nullable=false)
	private Long stateId;

	@Column(name="state_code", nullable=false, length=100)
	private String stateCode;

	@Column(name="state_name", nullable=false, length=100)
	private String stateName;

	//bi-directional many-to-one association to VenAddress
	/*
	@OneToMany(mappedBy="venState")
	private List<VenAddress> venAddresses;
	*/

    public VenState() {
    }

	public Long getStateId() {
		return this.stateId;
	}

	public void setStateId(Long stateId) {
		this.stateId = stateId;
	}

	public String getStateCode() {
		return this.stateCode;
	}

	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}

	public String getStateName() {
		return this.stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	/*
	public List<VenAddress> getVenAddresses() {
		return this.venAddresses;
	}

	public void setVenAddresses(List<VenAddress> venAddresses) {
		this.venAddresses = venAddresses;
	}
	*/
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (!(obj instanceof VenState)) {
			return false;
		}
		
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		
		VenState venState = (VenState) obj;
		
		if ((this.stateCode != null) && (venState.getStateCode() != null) && (!this.stateCode.equalsIgnoreCase(venState.getStateCode()))) {
			return false;
		}
		if ((this.stateName != null) && (venState.getStateName() != null) && (!this.stateName.equalsIgnoreCase(venState.getStateName()))) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		
		result = prime * result + (stateId != null ? stateId.hashCode() : 0);
		result = prime * result + (stateCode != null ? stateCode.hashCode() : 0);
		result = prime * result + (stateName != null ? stateName.hashCode() : 0);
		
		return result;
	}
	
}