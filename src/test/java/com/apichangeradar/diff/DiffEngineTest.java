package com.apichangeradar.diff;

import com.apichangeradar.common.domain.ChangeSeverity;
import com.apichangeradar.common.domain.ChangeType;
import com.apichangeradar.diff.domain.ChangeReport;
import com.apichangeradar.diff.service.DiffService;
import com.apichangeradar.diff.util.SwaggerModelMapper;
import com.apichangeradar.snapshot.domain.SpecSnapshot;
import com.apichangeradar.apiregistry.domain.MonitoredApi;
import com.apichangeradar.diff.repository.ChangeItemRepository;
import com.apichangeradar.diff.repository.ChangeReportRepository;
import com.apichangeradar.snapshot.repository.SpecSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class DiffEngineTest {
    @Test
    void detectsRemovedFieldAndTypeChange() throws Exception {
        SwaggerModelMapper mapper = new SwaggerModelMapper();
        ChangeReportRepository rr = Mockito.mock(ChangeReportRepository.class);
        ChangeItemRepository ir = Mockito.mock(ChangeItemRepository.class);
        SpecSnapshotRepository sr = Mockito.mock(SpecSnapshotRepository.class);
        DiffService svc = new DiffService(mapper, rr, ir, sr);

        String v1 = Files.readString(Paths.get("src/test/resources/openapi/payment-api-v1.json"));
        String v2 = Files.readString(Paths.get("src/test/resources/openapi/payment-api-v2.json"));

        SpecSnapshot s1 = new SpecSnapshot();
        s1.setId(1L);
        s1.setVersion(1);
        s1.setContent(v1);
        SpecSnapshot s2 = new SpecSnapshot();
        s2.setId(2L);
        s2.setVersion(2);
        s2.setContent(v2);
        MonitoredApi api = new MonitoredApi();
        api.setId(10L);
        s1.setMonitoredApi(api);
        s2.setMonitoredApi(api);

        ChangeReport r = svc.compare(s1, s2);
        assertThat(r).isNotNull();
        assertThat(r.getBreakingCount() + r.getNonBreakingCount() + r.getDeprecationCount()).isGreaterThan(0);
        assertThat(r.getChangeItems()).anyMatch(ci -> ci.getType() == ChangeType.FIELD_REMOVED && ci.getSeverity() == ChangeSeverity.BREAKING);
        assertThat(r.getChangeItems()).anyMatch(ci -> ci.getType() == ChangeType.TYPE_CHANGED && ci.getSeverity() == ChangeSeverity.BREAKING);
        assertThat(r.getChangeItems()).anyMatch(ci -> ci.getType() == ChangeType.OPTIONAL_FIELD_ADDED && ci.getSeverity() == ChangeSeverity.NON_BREAKING);
    }
}

