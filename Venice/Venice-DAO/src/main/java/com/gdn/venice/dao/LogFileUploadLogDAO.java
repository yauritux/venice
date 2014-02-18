package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.LogFileUploadLog;

public interface LogFileUploadLogDAO extends JpaRepository<LogFileUploadLog, Long> {

}
