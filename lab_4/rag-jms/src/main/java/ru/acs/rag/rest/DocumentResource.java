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
import ru.acs.rag.entity.Document;
import ru.acs.rag.rest.dto.ChunkDto;
import ru.acs.rag.rest.dto.ChunkListDto;
import ru.acs.rag.rest.dto.DocumentDto;
import ru.acs.rag.rest.dto.DocumentListDto;
import ru.acs.rag.service.ChunkService;
import ru.acs.rag.service.DocumentService;

import java.net.URI;
import java.util.List;


@Path("documents")
@RequestScoped
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class DocumentResource {

    @EJB
    private DocumentService documentService;
    @EJB
    private ChunkService chunkService;

    @Context
    private UriInfo uriInfo;

    @GET
    public DocumentListDto list() {
        DtoMapper map = new DtoMapper(uriInfo);
        DocumentListDto list = new DocumentListDto();
        list.setApiRoot(map.api());
        list.setHref(map.documentsHref());
        for (Document d : documentService.findAll()) {
            list.getItems().add(map.toSummary(d, d.getChunks().size()));
        }
        list.setCount(list.getItems().size());
        return list;
    }

    @GET
    @Path("{id}")
    public DocumentDto get(@PathParam("id") long id) {
        Document d = findOr404(id);
        return new DtoMapper(uriInfo).toDetail(d, chunkService.findByDocument(id));
    }

    @POST
    public Response create(@NotNull(message = "нет тела запроса") @Valid DocumentDto dto) {
        DtoMapper map = new DtoMapper(uriInfo);
        Document created = documentService.create(map.toEntity(dto));
        return Response.created(URI.create(map.documentHref(created.getId())))
                .entity(map.toDetail(created, List.of()))
                .build();
    }

    @PUT
    @Path("{id}")
    public DocumentDto update(@PathParam("id") long id,
                              @NotNull(message = "нет тела запроса") @Valid DocumentDto dto) {
        findOr404(id);
        DtoMapper map = new DtoMapper(uriInfo);
        Document changed = map.toEntity(dto);
        changed.setId(id);
        documentService.update(changed);
        return map.toDetail(documentService.find(id), chunkService.findByDocument(id));
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") long id) {
        findOr404(id);
        documentService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("{id}/chunks")
    public ChunkListDto chunks(@PathParam("id") long id) {
        Document d = findOr404(id);
        return new DtoMapper(uriInfo).toChunkList(d, chunkService.findByDocument(id));
    }

    @POST
    @Path("{id}/chunks")
    public Response addChunk(@PathParam("id") long id,
                             @NotNull(message = "нет тела запроса") @Valid ChunkDto dto) {
        findOr404(id);
        DtoMapper map = new DtoMapper(uriInfo);
        Chunk created = chunkService.create(id, map.toEntity(dto));
        return Response.created(URI.create(map.chunkHref(created.getId())))
                .entity(map.toDto(chunkService.find(created.getId())))
                .build();
    }


    @POST
    @Path("{id}/rechunk")
    @Consumes(MediaType.WILDCARD)
    public ChunkListDto rechunk(@PathParam("id") long id,
                                @QueryParam("size") @DefaultValue("40") int size,
                                @QueryParam("overlap") @DefaultValue("10") int overlap) {
        findOr404(id);
        documentService.rechunk(id, size, overlap);
        return chunks(id);
    }

    private Document findOr404(long id) {
        Document d = documentService.find(id);
        if (d == null) {
            throw new NotFoundException("Документ " + id + " не найден");
        }
        return d;
    }
}
