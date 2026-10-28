package ru.acs.rag.rest;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import ru.acs.rag.rest.dto.SearchResultDto;
import ru.acs.rag.service.ChunkService;
import ru.acs.rag.service.SearchHit;
import ru.acs.rag.service.SimpleTokenizer;

@Path("search")
@RequestScoped
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class SearchResource {

    @EJB
    private ChunkService chunkService;

    @Context
    private UriInfo uriInfo;

    @GET
    public SearchResultDto search(@QueryParam("q") String q, @QueryParam("topK") @DefaultValue("3") int topK) {
        if (q == null || q.isBlank()) {
            throw new BadRequestException("Параметр q (текст запроса) обязателен");
        }
        if (topK < 1) {
            throw new BadRequestException("topK должен быть не меньше 1");
        }
        DtoMapper map = new DtoMapper(uriInfo);
        SearchResultDto result = new SearchResultDto();
        result.setApiRoot(map.api());
        result.setHref(uriInfo.getRequestUri().toString());
        result.setQuery(q);
        result.setTopK(topK);
        result.setQueryTokens(SimpleTokenizer.countTokens(q));
        for (SearchHit hit : chunkService.search(q, topK)) {
            result.getHits().add(new SearchResultDto.Hit(hit.getScore(), map.toDto(hit.getChunk())));
            result.setContextTokens(result.getContextTokens() + hit.getChunk().getTokenCount());
        }
        return result;
    }
}
