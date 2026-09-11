package com.battleforge.exception;

/**
 * Also thrown when a resource exists but belongs to another user: ownership failures
 * answer 404 rather than 403 so the API never confirms that an id exists.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException("%s not found: %s".formatted(resource, id));
    }
}
