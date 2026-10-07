package com.apichangeradar.diff.service;

import com.apichangeradar.common.domain.ChangeSeverity;
import com.apichangeradar.common.domain.ChangeType;
import com.apichangeradar.diff.domain.ChangeItem;
import com.apichangeradar.diff.domain.ChangeReport;
import com.apichangeradar.diff.domain.diffmodel.*;
import com.apichangeradar.diff.dto.ChangeItemDto;
import com.apichangeradar.diff.dto.ChangeReportDto;
import com.apichangeradar.diff.repository.ChangeItemRepository;
import com.apichangeradar.diff.repository.ChangeReportRepository;
import com.apichangeradar.diff.util.SwaggerModelMapper;
import com.apichangeradar.snapshot.domain.SpecSnapshot;
import com.apichangeradar.snapshot.repository.SpecSnapshotRepository;
import com.apichangeradar.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class DiffService {
    private final SwaggerModelMapper mapper;
    private final ChangeReportRepository reportRepo;
    private final ChangeItemRepository itemRepo;
    private final SpecSnapshotRepository snapRepo;

    public ChangeReport compare(SpecSnapshot oldSnap, SpecSnapshot newSnap) {
        ApiSchema oldApi = mapper.map(oldSnap.getContent());
        ApiSchema newApi = mapper.map(newSnap.getContent());
        List<ChangeItem> items = new ArrayList<>();
        Set<String> oldPaths = oldApi.getPaths().keySet();
        Set<String> newPaths = newApi.getPaths().keySet();
        for (String p : oldPaths) {
            if (!newPaths.contains(p)) {
                items.add(item(ChangeType.ENDPOINT_REMOVED, ChangeSeverity.BREAKING, p, "Endpoint removed"));
                continue;
            }
            PathItem opOld = oldApi.getPaths().get(p);
            PathItem opNew = newApi.getPaths().get(p);
            Set<String> mOld = opOld.getOperations().keySet();
            Set<String> mNew = opNew.getOperations().keySet();
            for (String m : mOld) {
                if (!mNew.contains(m)) {
                    items.add(item(ChangeType.METHOD_REMOVED, ChangeSeverity.BREAKING, m + " " + p, "Method removed"));
                } else {
                    compareParams(opOld.getOperations().get(m), opNew.getOperations().get(m), items);
                    compareSchemas("response", opOld.getOperations().get(m).getResponseSchema(), opNew.getOperations().get(m).getResponseSchema(), m + " " + p, items);
                    compareSchemas("requestBody", opOld.getOperations().get(m).getRequestBodySchema(), opNew.getOperations().get(m).getRequestBodySchema(), m + " " + p, items);
                }
            }
            for (String m : mNew) {
                if (!mOld.contains(m)) {
                    items.add(item(ChangeType.OPTIONAL_ENDPOINT_ADDED, ChangeSeverity.NON_BREAKING, m + " " + p, "Method added"));
                }
            }
        }
        for (String p : newPaths) {
            if (!oldPaths.contains(p)) {
                items.add(item(ChangeType.OPTIONAL_ENDPOINT_ADDED, ChangeSeverity.NON_BREAKING, p, "Endpoint added"));
            }
        }
        ChangeReport r = new ChangeReport();
        r.setFromSnapshotId(oldSnap.getId());
        r.setToSnapshotId(newSnap.getId());
        r.setFromVersion(oldSnap.getVersion());
        r.setToVersion(newSnap.getVersion());
        r.setChangeItems(items);
        r.setBreakingCount((int)items.stream().filter(i -> i.getSeverity()==ChangeSeverity.BREAKING).count());
        r.setNonBreakingCount((int)items.stream().filter(i -> i.getSeverity()==ChangeSeverity.NON_BREAKING).count());
        r.setDeprecationCount((int)items.stream().filter(i -> i.getSeverity()==ChangeSeverity.DEPRECATION).count());
        r.setHasBreakingChanges(r.getBreakingCount()>0);
        for (ChangeItem ci : items) ci.setChangeReport(r);
        return r;
    }
    private void compareParams(Operation oOld, Operation oNew, List<ChangeItem> items) {
        String ctx = oNew.getMethod()+" "+oNew.getPath();
        for (Param p : oOld.getParameters()) {
            boolean found=false;
            for (Param pn : oNew.getParameters()) if (pn.getName().equals(p.getName()) && pn.getIn().equals(p.getIn())) { found=true; break; }
            if (!found) items.add(item(ChangeType.FIELD_REMOVED, ChangeSeverity.BREAKING, ctx+"."+p.getName(), "Param removed"));
        }
        for (Param pn : oNew.getParameters()) {
            boolean found=false;
            for (Param p : oOld.getParameters()) if (pn.getName().equals(p.getName()) && pn.getIn().equals(p.getIn())) { found=true; break; }
            if (!found) {
                if (pn.isRequired()) items.add(item(ChangeType.REQUIRED_PARAM_ADDED, ChangeSeverity.BREAKING, ctx+"."+pn.getName(), "Required param added"));
                else items.add(item(ChangeType.OPTIONAL_FIELD_ADDED, ChangeSeverity.NON_BREAKING, ctx+"."+pn.getName(), "Optional param added"));
            }
        }
    }
    private void compareSchemas(String kind, SchemaNode sOld, SchemaNode sNew, String ctx, List<ChangeItem> items) {
        if (sOld==null || sNew==null) return;
        walk("", sOld, sNew, ctx+"."+kind, items);
    }
    private void walk(String prefix, SchemaNode oldN, SchemaNode newN, String ctx, List<ChangeItem> items) {
        if (oldN.getFields()==null || newN.getFields()==null) return;
        Map<String,Field> fOld = oldN.getFields();
        Map<String,Field> fNew = newN.getFields();
        for (String name : fOld.keySet()) {
            String path = prefix.isEmpty()?name:prefix+"."+name;
            if (!fNew.containsKey(name)) items.add(item(ChangeType.FIELD_REMOVED, ChangeSeverity.BREAKING, ctx+"."+path, "Field removed"));
            else {
                Field fo=fOld.get(name), fn=fNew.get(name);
                if (fo.getType()!=null && fn.getType()!=null && !Objects.equals(norm(fo.getType()), norm(fn.getType()))) {
                    items.add(item(ChangeType.TYPE_CHANGED, ChangeSeverity.BREAKING, ctx+"."+path, "Type changed"));
                }
            }
        }
        for (String name : fNew.keySet()) {
            if (!fOld.containsKey(name)) {
                String path = prefix.isEmpty()?name:prefix+"."+name;
                items.add(item(ChangeType.OPTIONAL_FIELD_ADDED, ChangeSeverity.NON_BREAKING, ctx+"."+path, "Field added"));
            }
        }
    }
    private String norm(String t) {
        if (t==null) return "unknown";
        t=t.toLowerCase();
        if (t.equals("integer")||t.equals("int32")||t.equals("int64")) return "integer";
        if (t.equals("number")||t.equals("float")||t.equals("double")) return "number";
        if (t.equals("string")||t.equals("date")||t.equals("date-time")) return "string";
        if (t.equals("boolean")) return "boolean";
        return t;
    }
    private ChangeItem item(ChangeType t, ChangeSeverity s, String path, String desc) {
        ChangeItem ci = new ChangeItem();
        ci.setType(t); ci.setSeverity(s); ci.setPath(path);
        ci.setDescription(desc.length()>500?desc.substring(0,497)+"...":desc);
        return ci;
    }
    @Transactional public ChangeReport saveReport(ChangeReport r){ return reportRepo.save(r); }
    @Transactional(readOnly=true) public List<ChangeReportDto> history(com.apichangeradar.apiregistry.domain.MonitoredApi api){ return reportRepo.findByMonitoredApiOrderByCreatedAtDesc(api).stream().map(this::toDto).toList(); }
    @Transactional(readOnly=true) public ChangeReportDto get(com.apichangeradar.apiregistry.domain.MonitoredApi api, Long id){ var r=reportRepo.findByIdAndMonitoredApi(id,api).orElseThrow(()->new NotFoundException("Report not found")); return toDto(r); }
    @Transactional public ChangeReportDto diffBetweenVersions(com.apichangeradar.apiregistry.domain.MonitoredApi api,int v1,int v2){ var list=snapRepo.findByMonitoredApiOrderByVersionAsc(api); var s1=list.stream().filter(s->s.getVersion()==v1).findFirst().orElseThrow(()->new NotFoundException("Snapshot not found")); var s2=list.stream().filter(s->s.getVersion()==v2).findFirst().orElseThrow(()->new NotFoundException("Snapshot not found")); var r=compare(s1,s2); r.setMonitoredApi(api); saveReport(r); return toDto(r); }
    private ChangeReportDto toDto(ChangeReport r){ return ChangeReportDto.builder().id(r.getId()).monitoredApiId(r.getMonitoredApi()!=null?r.getMonitoredApi().getId():null).fromSnapshotId(r.getFromSnapshotId()).toSnapshotId(r.getToSnapshotId()).fromVersion(r.getFromVersion()).toVersion(r.getToVersion()).breakingCount(r.getBreakingCount()).nonBreakingCount(r.getNonBreakingCount()).deprecationCount(r.getDeprecationCount()).hasBreakingChanges(r.isHasBreakingChanges()).createdAt(r.getCreatedAt()).items(r.getChangeItems().stream().map(ci->ChangeItemDto.builder().id(ci.getId()).type(ci.getType()).severity(ci.getSeverity()).path(ci.getPath()).description(ci.getDescription()).build()).toList()).build(); }
}

