package com.gdn.venice.persistence;

import java.io.Serializable;
import javax.persistence.*;


@Entity
@Table(name="ven_order_payment_installment_history")
public class VenOrderPaymentInstallmentHistory implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private VenOrderPaymentInstallmentHistoryPK id;

	@Column(name="history_reason", nullable=false, length=1000)
	private String historyReason;

	//bi-directional many-to-one association to VenOrderPayment
    @ManyToOne
	@JoinColumn(name="order_payment_id", nullable=false, insertable=false, updatable=false)
	private VenOrderPayment venOrderPayment;


    public VenOrderPaymentInstallmentHistory() {
    }

	public VenOrderPaymentInstallmentHistoryPK getId() {
		return this.id;
	}

	public void setId(VenOrderPaymentInstallmentHistoryPK id) {
		this.id = id;
	}
	
	public String getHistoryReason() {
		return this.historyReason;
	}

	public void setHistoryReason(String historyReason) {
		this.historyReason = historyReason;
	}

	public VenOrderPayment getVenOrderPayment() {
		return this.venOrderPayment;
	}

	public void setVenOrderPayment(VenOrderPayment venOrderPayment) {
		this.venOrderPayment = venOrderPayment;
	}
	
}