package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdBlacklistReason;

public interface FrdBlacklistReasonDAO extends JpaRepository<FrdBlacklistReason, Long> {

}
