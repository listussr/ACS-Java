package ru.acs.rag.rest;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import ru.acs.rag.entity.Chunk;
import ru.acs.rag.rest.dto.ChunkDto;
import ru.acs.rag.service.ChunkService;


@Path("chunks")
@RequestScoped
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ChunkResource {

    @EJB
    private ChunkService chunkService;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("{id}")
    public ChunkDto get(@PathParam("id") long id) {
        DtoMapper map = new DtoMapper(uriInfo);
        ChunkDto dto = map.toDto(findOr404(id));
        dto.setApiRoot(map.api());
        return dto;
    }

    @PUT
    @Path("{id}")
    public ChunkDto update(@PathParam("id") long id,
                           @NotNull(message = "нет тела запроса") @Valid ChunkDto dto) {
        findOr404(id);
        Chunk changed = new DtoMapper(uriInfo).toEntity(dto);
        changed.setId(id);
        chunkService.update(changed);
        return get(id);
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") long id) {
        findOr404(id);
        chunkService.delete(id);
        return Response.noContent().build();
    }

    private Chunk findOr404(long id) {
        Chunk c = chunkService.find(id);
        if (c == null) {
            throw new NotFoundException("Чанк " + id + " не найден");
        }
        return c;
    }
}
