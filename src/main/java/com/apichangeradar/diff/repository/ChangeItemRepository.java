package com.apichangeradar.diff.repository;

import com.apichangeradar.diff.domain.ChangeItem;
import com.apichangeradar.diff.domain.ChangeReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChangeItemRepository extends JpaRepository<ChangeItem,Long> {
    List<ChangeItem> findByChangeReport(ChangeReport r);
}

