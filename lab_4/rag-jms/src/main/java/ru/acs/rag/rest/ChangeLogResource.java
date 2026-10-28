package ru.acs.rag.rest;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import ru.acs.rag.entity.ChangeLog;
import ru.acs.rag.rest.dto.ChangeListDto;
import ru.acs.rag.service.ChangeLogService;


@Path("changes")
@RequestScoped
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ChangeLogResource {

    @EJB
    private ChangeLogService changeLogService;

    @Context
    private UriInfo uriInfo;

    @GET
    public ChangeListDto list(@QueryParam("entity") String entity,
                              @QueryParam("entityId") Long entityId,
                              @QueryParam("limit") @DefaultValue("50") int limit) {
        if (limit < 1 || limit > 1000) {
            throw new BadRequestException("limit должен быть от 1 до 1000");
        }
        DtoMapper map = new DtoMapper(uriInfo);
        ChangeListDto list = new ChangeListDto();
        list.setApiRoot(map.api());
        list.setHref(uriInfo.getRequestUri().toString());
        for (ChangeLog c : changeLogService.find(entity, entityId, limit)) {
            ChangeListDto.Change dto = new ChangeListDto.Change();
            dto.setId(c.getId());
            dto.setType(c.getChangeType());
            dto.setEntityClass(c.getEntityClass());
            dto.setEntityId(c.getEntityId());
            dto.setEntityHref("Document".equals(c.getEntityClass())
                    ? map.documentHref(c.getEntityId()) : map.chunkHref(c.getEntityId()));
            dto.setChangedAt(c.getChangedAt().withNano(0).toString());
            dto.setLoggedAt(c.getLoggedAt().withNano(0).toString());
            dto.setDetails(c.getDetails());
            list.getItems().add(dto);
        }
        list.setCount(list.getItems().size());
        return list;
    }
}
