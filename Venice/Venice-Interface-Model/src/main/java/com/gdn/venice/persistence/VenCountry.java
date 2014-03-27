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
 * The persistent class for the ven_country database table.
 *
 * Change History:
 *  March 27, 2014 (1:59PM) - yauritux : 
 *    - override equals and hashCode methods 
 *    - removes bidirectional association (one-to-many)
 */
@Entity
@Table(name="ven_country")
public class VenCountry implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
@GeneratedValue(strategy=GenerationType.TABLE, generator="ven_country")  
	@TableGenerator(name="ven_country", table="openjpaseq", pkColumnName="id", valueColumnName="sequence_value", allocationSize=1)  //flush every 1 insert
	@Column(name="country_id", unique=true, nullable=false)
	private Long countryId;

	@Column(name="country_code", nullable=false, length=100)
	private String countryCode;

	@Column(name="country_name", nullable=false, length=100)
	private String countryName;

	//bi-directional many-to-one association to VenAddress
	/*
	@OneToMany(mappedBy="venCountry")
	private List<VenAddress> venAddresses;
	*/

    public VenCountry() {
    }

	public Long getCountryId() {
		return this.countryId;
	}

	public void setCountryId(Long countryId) {
		this.countryId = countryId;
	}

	public String getCountryCode() {
		return this.countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getCountryName() {
		return this.countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
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
		
		if (!(obj instanceof VenCountry)) {
			return false;
		}
		
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		
		VenCountry venCountry = (VenCountry) obj;
		
		if ((this.countryCode != null) && (venCountry.getCountryCode() != null) && (!this.countryCode.equalsIgnoreCase(venCountry.getCountryCode()))) {
			return false;
		}
		
		if ((this.countryName != null) && (venCountry.getCountryName() != null) && (!this.countryName.equalsIgnoreCase(venCountry.getCountryName()))) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		
		result = prime * result + (countryId != null ? countryId.hashCode() : 0);
		result = prime * result + (countryCode != null ? countryCode.hashCode() : 0);
		result = prime * result + (countryName != null ? countryName.hashCode() : 0);
		
		return result;
	}
	
}