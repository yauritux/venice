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
 * The persistent class for the ven_city database table.
 * Change History:
 *  March 27, 2014 (1:59PM) - yauritux : 
 *    - override equals and hashCode methods 
 *    - removes bidirectional association (one-to-many)
 * 
 */
@Entity
@Table(name="ven_city")
public class VenCity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
@GeneratedValue(strategy=GenerationType.TABLE, generator="ven_city")  
	@TableGenerator(name="ven_city", table="openjpaseq", pkColumnName="id", valueColumnName="sequence_value", allocationSize=1)  //flush every 1 insert
	@Column(name="city_id", unique=true, nullable=false)
	private Long cityId;

	@Column(name="city_code", nullable=false, length=100)
	private String cityCode;

	@Column(name="city_name", nullable=false, length=100)
	private String cityName;

	//bi-directional many-to-one association to VenAddress
	/*
	@OneToMany(mappedBy="venCity")
	private List<VenAddress> venAddresses;
	*/

    public VenCity() {
    }

	public Long getCityId() {
		return this.cityId;
	}

	public void setCityId(Long cityId) {
		this.cityId = cityId;
	}

	public String getCityCode() {
		return this.cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getCityName() {
		return this.cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
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
		
		if (!(obj instanceof VenCity)) {
			return false;
		}
		
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		
		VenCity venCity = (VenCity) obj;
		
		if ((this.cityCode != null) && (venCity.getCityCode() != null) && (!this.cityCode.equalsIgnoreCase(venCity.getCityCode()))) {
			return false;
		}
		
		if ((this.cityName != null) && (venCity.getCityName() != null) && (!this.cityCode.equalsIgnoreCase(venCity.getCityCode()))) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		
		result = prime * result + (cityId != null ? cityId.hashCode() : 0);
		result = prime * result + (cityCode != null ? cityCode.hashCode() : 0);
		result = prime * result + (cityName != null ? cityName.hashCode() : 0);
		
		return result;
	}
	
}