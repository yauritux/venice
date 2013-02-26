package com.gdn.venice.persistence;

import java.io.Serializable;
import javax.persistence.*;

import java.util.List;


/**
 * The persistent class for the raf_user database table.
 * 
 */
@Entity
@Table(name="raf_user")
public class RafUser implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
@GeneratedValue(strategy=GenerationType.TABLE, generator="raf_user")  
	@TableGenerator(name="raf_user", table="openjpaseq", pkColumnName="id", valueColumnName="sequence_value", allocationSize=1)  //flush every 1 insert
	@Column(name="user_id")
	private Long userId;
	
	@Column(name="login_name", nullable=false, length=100)
	private String loginName;

	@Column(name="name", length=100)
	private String name;
	
	@Column(name="add_to_stockholm")
	private Boolean addToStockholm;
	
	@Column(name="department")
	private String department;

	//bi-directional many-to-one association to VenParty
    @ManyToOne
	@JoinColumn(name="party_id")
	private VenParty venParty;

	//bi-directional many-to-one association to RafUserGroupMembership
	@OneToMany(mappedBy="rafUser")
	private List<RafUserGroupMembership> rafUserGroupMemberships;

    public RafUser() {
    }

	public Long getUserId() {
		return this.userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLoginName() {
		return this.loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public VenParty getVenParty() {
		return this.venParty;
	}

	public void setVenParty(VenParty venParty) {
		this.venParty = venParty;
	}
	
	public List<RafUserGroupMembership> getRafUserGroupMemberships() {
		return this.rafUserGroupMemberships;
	}

	public void setRafUserGroupMemberships(List<RafUserGroupMembership> rafUserGroupMemberships) {
		this.rafUserGroupMemberships = rafUserGroupMemberships;
	}

	public Boolean getAddToStockholm() {
		return addToStockholm;
	}

	public void setAddToStockholm(Boolean addToStockholm) {
		this.addToStockholm = addToStockholm;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}
	
}