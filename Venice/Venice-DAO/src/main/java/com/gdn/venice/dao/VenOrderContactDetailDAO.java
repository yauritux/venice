package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderContactDetail;

/**
 * 
 * @author yauritux
 *
 */
public interface VenOrderContactDetailDAO extends JpaRepository<VenOrderContactDetail, Long>{
	
	public static final String FIND_BY_CONTACTTYPEEMAIL_VENORDER =
		"SELECT o " +
		"FROM VenOrderContactDetail o " +
		"JOIN FETCH o.venContactDetail cd " +
		"WHERE cd.venContactDetailType.contactDetailTypeId = 3 " +
		"AND o.venOrder.orderId = ?1";
	
	public static final String FIND_BY_CONTACTTYPEPHONE_VENORDER =
		"SELECT o " +
		"FROM VenOrderContactDetail o " +
		"JOIN FETCH o.venContactDetail cd " +
		"WHERE cd.venContactDetailType.contactDetailTypeId = 0 " +
		"AND o.venOrder = ?1";
	
	public static final String FIND_BY_CONTACTTYPEMOBILE_CONTACTTYPEPHONE_VENORDER =
		"SELECT o " +
		"FROM VenOrderContactDetail o " +
		"JOIN FETCH o.venContactDetail cd " +
		"WHERE (cd.venContactDetailType.contactDetailTypeId = 1 " +
		"    OR cd.venContactDetailType.contactDetailTypeId = 0 " +
		"       ) " +
		"AND o.venOrder = ?1";
	
	public static final String FIND_BY_CONTACTTYPEMOBILE_VENORDER =
		"SELECT o " +
		"FROM VenOrderContactDetail o " +
		"JOIN FETCH o.venContactDetail cd " +
		"WHERE cd.venContactDetailType.contactDetailTypeId = 1 " +
		"AND o.venOrder = ?1";

	public static final String FIND_BY_VENORDERORDERID_AND_VENCONTACTDETAILVENCONTACTDETAILTYPECONTACTDETAILTYPEID =
	"SELECT o " +
	"FROM VenOrderContactDetail o " +
	"JOIN FETCH o.venContactDetail cd " +
	"WHERE o.venOrder = ?1 " +
	"AND (cd.venContactDetailType.contactDetailTypeId = ?2 " +
	"OR cd.venContactDetailType.contactDetailTypeId = ?3 " +
	"OR cd.venContactDetailType.contactDetailTypeId = ?4)";
	
	public static final String FIND_BY_VENORDERORDERID_AND_VENCONTACTDETAILCONTACTDETAIL=
	"SELECT o " +
	"FROM VenOrderContactDetail o " +
	"JOIN FETCH o.venContactDetail cd " +
	"WHERE o.venOrder = ?1 " +
	"AND cd.contactDetail = ?2";
		
	
	@Query(FIND_BY_CONTACTTYPEEMAIL_VENORDER)
	public VenOrderContactDetail findByContactEmailVenOrder(long orderId);
	
	@Query(FIND_BY_CONTACTTYPEPHONE_VENORDER)
	public List<VenOrderContactDetail> findByContactPhoneVenOrder(VenOrder order);
	
	@Query(FIND_BY_CONTACTTYPEMOBILE_CONTACTTYPEPHONE_VENORDER)
	public List<VenOrderContactDetail> findByContactMobileContactPhoneVenOrder(VenOrder order);
	
	@Query(FIND_BY_CONTACTTYPEMOBILE_VENORDER)
	public List<VenOrderContactDetail> findByContactMobileVenOrder(VenOrder order);
	
	@Query(FIND_BY_VENORDERORDERID_AND_VENCONTACTDETAILVENCONTACTDETAILTYPECONTACTDETAILTYPEID)
	public List<VenOrderContactDetail> findByVenOrderOrderIdAndVenContactDetailVenContactDetailTypeContactDetailTypeId(VenOrder order, long  phone, long  mobile, long email);

	@Query(FIND_BY_VENORDERORDERID_AND_VENCONTACTDETAILCONTACTDETAIL)
	public List<VenOrderContactDetail> findByVenOrderOrderIdAndVenContactDetailContactDetail(VenOrder order, String contactDetail);
}
