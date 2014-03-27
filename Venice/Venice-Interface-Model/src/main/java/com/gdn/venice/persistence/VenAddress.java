package com.gdn.venice.persistence;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;


/**
 * The persistent class for the ven_address database table.
 * 
 * Change History:
 * March 27, 2014 (1:19PM) : 
 *   - override equals and hashCode logic to compare two addresses (yauritux)
 *   - removes bidirectional association (one-to-many)
 * 
 */
@Entity
@Table(name="ven_address")
public class VenAddress implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
@GeneratedValue(strategy=GenerationType.TABLE, generator="ven_address")  
	@TableGenerator(name="ven_address", table="openjpaseq", pkColumnName="id", valueColumnName="sequence_value", allocationSize=1)  //flush every 1 insert
	@Column(name="address_id", unique=true, nullable=false)
	private Long addressId;

	@Column(length=100)
	private String kecamatan;

	@Column(length=100)
	private String kelurahan;

	@Column(name="postal_code", length=100)
	private String postalCode;

	@Column(name="street_address_1", length=1000)
	private String streetAddress1;

	@Column(name="street_address_2", length=1000)
	private String streetAddress2;

	//bi-directional many-to-one association to LogMerchantPickupInstruction
	/*
	@OneToMany(mappedBy="venAddress")
	private List<LogMerchantPickupInstruction> logMerchantPickupInstructions;
	*/

	//bi-directional many-to-one association to VenCity
    @ManyToOne
	@JoinColumn(name="city_id")
	private VenCity venCity;

	//bi-directional many-to-one association to VenCountry
    @ManyToOne
	@JoinColumn(name="country_id")//, nullable=false)
	private VenCountry venCountry;

	//bi-directional many-to-one association to VenState
    @ManyToOne
	@JoinColumn(name="state_id")
	private VenState venState;

	//bi-directional many-to-one association to VenOrderItem
    /*
	@OneToMany(mappedBy="venAddress")
	private List<VenOrderItem> venOrderItems;
	*/

	//bi-directional many-to-one association to VenOrderPayment
    /*
	@OneToMany(mappedBy="venAddress")
	private List<VenOrderPayment> venOrderPayments;
	*/

	//bi-directional many-to-one association to VenPartyAddress
	/*
	@OneToMany(mappedBy="venAddress")
	private List<VenPartyAddress> venPartyAddresses;
	*/

    public VenAddress() {
    }

	public Long getAddressId() {
		return this.addressId;
	}

	public void setAddressId(Long addressId) {
		this.addressId = addressId;
	}

	public String getKecamatan() {
		return this.kecamatan;
	}

	public void setKecamatan(String kecamatan) {
		this.kecamatan = kecamatan;
	}

	public String getKelurahan() {
		return this.kelurahan;
	}

	public void setKelurahan(String kelurahan) {
		this.kelurahan = kelurahan;
	}

	public String getPostalCode() {
		return this.postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getStreetAddress1() {
		return this.streetAddress1;
	}

	public void setStreetAddress1(String streetAddress1) {
		this.streetAddress1 = streetAddress1;
	}

	public String getStreetAddress2() {
		return this.streetAddress2;
	}

	public void setStreetAddress2(String streetAddress2) {
		this.streetAddress2 = streetAddress2;
	}

	/*
	public List<LogMerchantPickupInstruction> getLogMerchantPickupInstructions() {
		return this.logMerchantPickupInstructions;
	}

	public void setLogMerchantPickupInstructions(List<LogMerchantPickupInstruction> logMerchantPickupInstructions) {
		this.logMerchantPickupInstructions = logMerchantPickupInstructions;
	}
	*/
	
	public VenCity getVenCity() {
		return this.venCity;
	}

	public void setVenCity(VenCity venCity) {
		this.venCity = venCity;
	}
	
	public VenCountry getVenCountry() {
		return this.venCountry;
	}

	public void setVenCountry(VenCountry venCountry) {
		this.venCountry = venCountry;
	}
	
	public VenState getVenState() {
		return this.venState;
	}

	public void setVenState(VenState venState) {
		this.venState = venState;
	}
	
	/*
	public List<VenOrderItem> getVenOrderItems() {
		return this.venOrderItems;
	}

	public void setVenOrderItems(List<VenOrderItem> venOrderItems) {
		this.venOrderItems = venOrderItems;
	}
	*/
	
	/*
	public List<VenOrderPayment> getVenOrderPayments() {
		return this.venOrderPayments;
	}

	public void setVenOrderPayments(List<VenOrderPayment> venOrderPayments) {
		this.venOrderPayments = venOrderPayments;
	}
	*/
	
	/*
	public List<VenPartyAddress> getVenPartyAddresses() {
		return this.venPartyAddresses;
	}

	public void setVenPartyAddresses(List<VenPartyAddress> venPartyAddresses) {
		this.venPartyAddresses = venPartyAddresses;
	}
	*/
	
	@Override
	/**
	 * This method will be comparing 2 provided addresses whether equal or not
	 * based on their particular values such as : city, country, state, etc.
	 * 
	 * @param obj
	 * @return true if both objects are equal, otherwise is false will be returned
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if (!(obj instanceof VenAddress)) {
			return false;
		}
		
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		
		VenAddress venAddress = (VenAddress) obj;
		
		if ((this.kecamatan != null) && (venAddress.getKecamatan() != null) && (!this.kecamatan.equalsIgnoreCase(venAddress.getKecamatan()))) {
			return false;
		}
		if ((this.kelurahan != null) && (venAddress.getKelurahan() != null) && (!this.kelurahan.equalsIgnoreCase(venAddress.getKelurahan()))) {
			return false;
		}
		if ((this.postalCode != null) && (venAddress.getPostalCode() != null) && (!this.postalCode.equalsIgnoreCase(venAddress.getPostalCode()))) {
			return false;
		}
		if ((this.streetAddress1 != null) && (venAddress.getStreetAddress1() != null) && (!this.streetAddress1.equalsIgnoreCase(venAddress.getStreetAddress1()))) {
			return false;
		}
		if ((this.streetAddress2 != null) && (venAddress.getStreetAddress2() != null) && (!this.streetAddress2.equalsIgnoreCase(venAddress.getStreetAddress2()))) {
			return false;
		}
		if ((this.venCity != null) && (venAddress.getVenCity() != null) && (!this.venCity.equals(venAddress.getVenCity()))) {
			return false;
		}
		if ((this.venCountry != null) && (venAddress.getVenCountry() != null) && (!this.venCountry.equals(venAddress.getVenCountry()))) {
			return false;
		}
		if ((this.venState != null) && (venAddress.getVenState() != null) && (!this.venState.equals(venAddress.getVenState()))) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + (addressId != null ? addressId.hashCode() : 0);
		result = prime * result + (kelurahan != null ? kelurahan.hashCode() : 0);
		result = prime * result + (kecamatan != null ? kecamatan.hashCode() : 0);
		result = prime * result + (postalCode != null ? postalCode.hashCode() : 0);
		result = prime * result + (streetAddress1 != null ? streetAddress1.hashCode() : 0);
		result = prime * result + (streetAddress2 != null ? streetAddress2.hashCode() : 0);
		result = prime * result + (venCity != null ? venCity.hashCode() : 0);
		result = prime * result + (venCountry != null ? venCountry.hashCode() : 0);
		result = prime * result + (venState != null ? venState.hashCode() : 0);
		return result;
	}			
}