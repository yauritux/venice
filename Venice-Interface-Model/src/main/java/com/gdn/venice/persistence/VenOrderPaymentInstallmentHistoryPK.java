package com.gdn.venice.persistence;

import java.io.Serializable;
import javax.persistence.*;

@Embeddable
public class VenOrderPaymentInstallmentHistoryPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="order_payment_id", unique=true, nullable=false)
	private Long orderPaymentId;

    @Temporal( TemporalType.TIMESTAMP)
	@Column(name="installment_timestamp", unique=true, nullable=false)
	private java.util.Date installmentTimestamp;

    public VenOrderPaymentInstallmentHistoryPK() {
    }
	public Long getOrderPaymentId() {
		return this.orderPaymentId;
	}
	public void setOrderPaymentId(Long orderPaymentId) {
		this.orderPaymentId = orderPaymentId;
	}
	public java.util.Date getInstallmentTimestamp() {
		return this.installmentTimestamp;
	}
	public void setInstallmentTimestamp(java.util.Date installmentTimestamp) {
		this.installmentTimestamp = installmentTimestamp;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof VenOrderPaymentInstallmentHistoryPK)) {
			return false;
		}
		VenOrderPaymentInstallmentHistoryPK castOther = (VenOrderPaymentInstallmentHistoryPK)other;
		return 
			this.orderPaymentId.equals(castOther.orderPaymentId)
			&& this.installmentTimestamp.equals(castOther.installmentTimestamp);

    }
    
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.orderPaymentId.hashCode();
		hash = hash * prime + this.installmentTimestamp.hashCode();
		
		return hash;
    }
}