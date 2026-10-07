package com.apichangeradar.diff.util;

import com.apichangeradar.diff.domain.diffmodel.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.springframework.stereotype.Component;

@Component
public class SwaggerModelMapper {
    public ApiSchema map(String content) {
        ParseOptions opts = new ParseOptions();
        opts.setResolveFully(false);
        opts.setResolveCombinators(false);
        OpenAPI openAPI = new OpenAPIV3Parser().readContents(content, null, opts).getOpenAPI();
        ApiSchema api = new ApiSchema();
        if (openAPI == null || openAPI.getPaths() == null) return api;
        openAPI.getPaths().forEach((p, pi) -> {
            com.apichangeradar.diff.domain.diffmodel.PathItem item = new com.apichangeradar.diff.domain.diffmodel.PathItem();
            item.setPath(p);
            if (pi.getGet() != null) item.getOperations().put("GET", op("GET", p, pi.getGet()));
            if (pi.getPost() != null) item.getOperations().put("POST", op("POST", p, pi.getPost()));
            if (pi.getPut() != null) item.getOperations().put("PUT", op("PUT", p, pi.getPut()));
            if (pi.getPatch() != null) item.getOperations().put("PATCH", op("PATCH", p, pi.getPatch()));
            if (pi.getDelete() != null) item.getOperations().put("DELETE", op("DELETE", p, pi.getDelete()));
            api.getPaths().put(p, item);
        });
        return api;
    }
    private com.apichangeradar.diff.domain.diffmodel.Operation op(String method, String path, io.swagger.v3.oas.models.Operation o) {
        com.apichangeradar.diff.domain.diffmodel.Operation op = new com.apichangeradar.diff.domain.diffmodel.Operation();
        op.setMethod(method); op.setPath(path);
        if (o.getParameters() != null) {
            for (Parameter pp : o.getParameters()) {
                Param p = new Param();
                p.setName(pp.getName());
                p.setIn(pp.getIn()==null?"query":pp.getIn());
                p.setRequired(pp.getRequired()!=null && pp.getRequired());
                p.setType(simpleType(pp.getSchema()));
                op.getParameters().add(p);
            }
        }
        if (o.getResponses()!=null && o.getResponses().get("200")!=null) {
            io.swagger.v3.oas.models.responses.ApiResponse r200 = o.getResponses().get("200");
            if (r200.getContent()!=null) {
                r200.getContent().values().stream().findFirst().ifPresent(mt -> op.setResponseSchema(schema(mt.getSchema())));
            }
        }
        if (o.getRequestBody()!=null && o.getRequestBody().getContent()!=null) {
            o.getRequestBody().getContent().values().stream().findFirst().ifPresent(mt -> op.setRequestBodySchema(schema(mt.getSchema())));
        }
        return op;
    }
    private SchemaNode schema(Schema<?> s) {
        if (s==null) return null;
        SchemaNode n = new SchemaNode();
        String type = s.getType();
        if (type==null && s.get$ref()!=null) type="object";
        n.setType(type==null?"object":type);
        if ("array".equals(n.getType()) && s.getItems()!=null) n.setItems(schema(s.getItems()));
        if (s instanceof ObjectSchema os && os.getProperties()!=null) {
            os.getProperties().forEach((k,v)-> {
                Field f=new Field(); f.setName(k); f.setType(simpleType(v));
                boolean req=os.getRequired()!=null && os.getRequired().contains(k); f.setRequired(req);
                n.getFields().put(k,f);
            });
        } else if (s.getProperties()!=null) {
            s.getProperties().forEach((k,v)-> {
                Field f=new Field(); f.setName(k); f.setType(simpleType(v)); f.setRequired(false);
                n.getFields().put(k,f);
            });
        }
        return n;
    }
    private String simpleType(Schema<?> s) {
        if (s==null) return "unknown";
        if (s.getType()!=null) return s.getType();
        if (s.get$ref()!=null) return "ref";
        return "object";
    }
}

