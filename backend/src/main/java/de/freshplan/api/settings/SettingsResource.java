package de.freshplan.api.settings;

import de.freshplan.infrastructure.settings.Setting;
import de.freshplan.infrastructure.settings.SettingsScope;
import de.freshplan.infrastructure.settings.SettingsService;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST API for Settings Registry (Sprint 1.2 PR #2).
 * Provides ETag-based caching and hierarchical settings resolution.
 */
@Path("/api/settings")
@Tag(name = "Settings", description = "Settings Registry with ETag support")
@RolesAllowed({"user", "admin"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SettingsResource {

    private static final Logger LOG = Logger.getLogger(SettingsResource.class);
    private static final String ETAG_HEADER = "ETag";
    private static final String IF_MATCH_HEADER = "If-Match";
    private static final String IF_NONE_MATCH_HEADER = "If-None-Match";
    private static final int CACHE_MAX_AGE_SECONDS = 60;

    @Inject
    SettingsService settingsService;

    @Context
    SecurityContext securityContext;

    /**
     * Gets a specific setting by scope and key.
     * Supports ETag-based caching with If-None-Match header.
     */
    @GET
    @Operation(summary = "Get a setting", description = "Retrieves a setting by scope and key with ETag support")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Setting found",
            headers = @Header(name = ETAG_HEADER, description = "Entity tag for caching"),
            content = @Content(schema = @Schema(implementation = SettingDto.class))
        ),
        @APIResponse(responseCode = "304", description = "Not Modified (ETag matched)"),
        @APIResponse(responseCode = "404", description = "Setting not found")
    })
    public Response getSetting(
        @QueryParam("scope") @Parameter(required = true, description = "Setting scope")
        SettingsScope scope,

        @QueryParam("scopeId") @Parameter(description = "Scope identifier (e.g., tenant ID, territory code)")
        String scopeId,

        @QueryParam("key") @Parameter(required = true, description = "Setting key")
        String key,

        @HeaderParam(IF_NONE_MATCH_HEADER) @Parameter(description = "ETag for conditional request")
        String ifNoneMatch
    ) {
        LOG.infof("GET setting: scope=%s, scopeId=%s, key=%s, ifNoneMatch=%s",
                 scope, scopeId, key, ifNoneMatch);

        Optional<Setting> setting = settingsService.getSetting(scope, scopeId, key);

        if (setting.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Setting s = setting.get();

        // Check ETag for conditional GET
        if (ifNoneMatch != null && s.matchesEtag(ifNoneMatch)) {
            LOG.debugf("ETag matched, returning 304 Not Modified");
            return Response.notModified()
                .header(ETAG_HEADER, s.etag)
                .cacheControl(getCacheControl())
                .build();
        }

        SettingDto dto = SettingDto.from(s);
        return Response.ok(dto)
            .header(ETAG_HEADER, s.etag)
            .cacheControl(getCacheControl())
            .build();
    }

    /**
     * Resolves a setting hierarchically based on context.
     */
    @GET
    @Path("/resolve/{key}")
    @Operation(summary = "Resolve setting hierarchically",
               description = "Resolves a setting using the scope hierarchy")
    public Response resolveSetting(
        @PathParam("key") String key,
        @QueryParam("tenantId") String tenantId,
        @QueryParam("territory") String territory,
        @QueryParam("accountId") String accountId,
        @QueryParam("contactRole") String contactRole,
        @HeaderParam(IF_NONE_MATCH_HEADER) String ifNoneMatch
    ) {
        LOG.infof("Resolving setting: key=%s, tenant=%s, territory=%s",
                 key, tenantId, territory);

        var context = new SettingsService.SettingsContext(
            tenantId, territory, accountId, contactRole
        );

        Optional<Setting> setting = settingsService.resolveSetting(key, context);

        if (setting.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Setting s = setting.get();

        if (ifNoneMatch != null && s.matchesEtag(ifNoneMatch)) {
            return Response.notModified()
                .header(ETAG_HEADER, s.etag)
                .cacheControl(getCacheControl())
                .build();
        }

        SettingDto dto = SettingDto.from(s);
        return Response.ok(dto)
            .header(ETAG_HEADER, s.etag)
            .cacheControl(getCacheControl())
            .build();
    }

    /**
     * Creates or updates a setting.
     * Uses If-Match header for optimistic locking on updates.
     */
    @PUT
    @Path("/{id}")
    @RolesAllowed("admin")
    @Transactional
    @Operation(summary = "Update a setting", description = "Updates a setting with optimistic locking")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Setting updated"),
        @APIResponse(responseCode = "404", description = "Setting not found"),
        @APIResponse(responseCode = "428", description = "Precondition Required (If-Match header missing)"),
        @APIResponse(responseCode = "412", description = "Precondition Failed (ETag mismatch)"),
        @APIResponse(responseCode = "409", description = "Conflict (concurrent modification)")
    })
    public Response updateSetting(
        @PathParam("id") UUID id,
        @HeaderParam(IF_MATCH_HEADER) @Parameter(description = "ETag for optimistic locking")
        String ifMatch,
        SettingUpdateDto update
    ) {
        LOG.infof("PUT setting: id=%s, ifMatch=%s", id, ifMatch);

        String userId = securityContext.getUserPrincipal().getName();

        try {
            Setting updated = settingsService.updateSettingWithEtag(
                id, update.value, update.metadata, ifMatch, userId
            );

            SettingDto dto = SettingDto.from(updated);
            return Response.ok(dto)
                .header(ETAG_HEADER, updated.etag)
                .build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            LOG.errorf(e, "Failed to update setting %s", id);
            throw new InternalServerErrorException("Failed to update setting", e);
        }
    }

    /**
     * Creates a new setting.
     */
    @POST
    @RolesAllowed("admin")
    @Transactional
    @Operation(summary = "Create a setting", description = "Creates a new setting")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Setting created"),
        @APIResponse(responseCode = "409", description = "Setting already exists")
    })
    public Response createSetting(SettingCreateDto create, @Context UriInfo uriInfo) {
        LOG.infof("POST setting: scope=%s, key=%s", create.scope, create.key);

        // Check if already exists
        Optional<Setting> existing = settingsService.getSetting(
            create.scope, create.scopeId, create.key
        );

        if (existing.isPresent()) {
            return Response.status(Response.Status.CONFLICT)
                .entity("Setting already exists")
                .build();
        }

        String userId = securityContext.getUserPrincipal().getName();
        Setting created = settingsService.saveSetting(
            create.scope, create.scopeId, create.key,
            create.value, create.metadata, userId
        );

        URI location = uriInfo.getAbsolutePathBuilder()
            .path(created.id.toString())
            .build();

        SettingDto dto = SettingDto.from(created);
        return Response.created(location)
            .entity(dto)
            .header(ETAG_HEADER, created.etag)
            .build();
    }

    /**
     * Lists settings for a scope.
     */
    @GET
    @Path("/list")
    @Operation(summary = "List settings", description = "Lists all settings for a given scope")
    public Response listSettings(
        @QueryParam("scope") @Parameter(required = true) SettingsScope scope,
        @QueryParam("scopeId") String scopeId
    ) {
        LOG.infof("Listing settings: scope=%s, scopeId=%s", scope, scopeId);

        List<Setting> settings = settingsService.listSettings(scope, scopeId);
        List<SettingDto> dtos = settings.stream()
            .map(SettingDto::from)
            .toList();

        return Response.ok(dtos)
            .cacheControl(getCacheControl())
            .build();
    }

    /**
     * Gets cache statistics.
     */
    @GET
    @Path("/stats/cache")
    @RolesAllowed("admin")
    @Operation(summary = "Get cache statistics", description = "Returns cache performance metrics")
    public Response getCacheStats() {
        var stats = settingsService.getCacheStats();
        return Response.ok(stats).build();
    }

    /**
     * Deletes a setting.
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin")
    @Transactional
    @Operation(summary = "Delete a setting", description = "Deletes a setting by ID")
    public Response deleteSetting(@PathParam("id") UUID id) {
        LOG.infof("DELETE setting: id=%s", id);

        boolean deleted = settingsService.deleteSetting(id);
        if (deleted) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    private CacheControl getCacheControl() {
        CacheControl cc = new CacheControl();
        cc.setMaxAge(CACHE_MAX_AGE_SECONDS);
        cc.setPrivate(false);
        cc.setMustRevalidate(true);
        return cc;
    }

    /**
     * DTO for setting responses.
     */
    @Schema(description = "Setting with metadata and ETag")
    public record SettingDto(
        @Schema(description = "Unique identifier")
        UUID id,

        @Schema(description = "Setting scope", required = true)
        SettingsScope scope,

        @Schema(description = "Scope identifier")
        String scopeId,

        @Schema(description = "Setting key", required = true)
        String key,

        @Schema(description = "Setting value as JSON", required = true, type = SchemaType.OBJECT)
        JsonObject value,

        @Schema(description = "Additional metadata", type = SchemaType.OBJECT)
        JsonObject metadata,

        @Schema(description = "Entity tag for caching")
        String etag,

        @Schema(description = "Version number for optimistic locking")
        Integer version,

        @Schema(description = "Last update timestamp")
        String updatedAt
    ) {
        public static SettingDto from(Setting setting) {
            return new SettingDto(
                setting.id,
                setting.scope,
                setting.scopeId,
                setting.key,
                setting.value,
                setting.metadata,
                setting.etag,
                setting.version,
                setting.updatedAt != null ? setting.updatedAt.toString() : null
            );
        }
    }

    /**
     * DTO for creating settings.
     */
    @Schema(description = "Request to create a new setting")
    public record SettingCreateDto(
        @Schema(required = true) SettingsScope scope,
        @Schema String scopeId,
        @Schema(required = true) String key,
        @Schema(required = true, type = SchemaType.OBJECT) JsonObject value,
        @Schema(type = SchemaType.OBJECT) JsonObject metadata
    ) {}

    /**
     * DTO for updating settings.
     */
    @Schema(description = "Request to update a setting")
    public record SettingUpdateDto(
        @Schema(required = true, type = SchemaType.OBJECT) JsonObject value,
        @Schema(type = SchemaType.OBJECT) JsonObject metadata
    ) {}
}