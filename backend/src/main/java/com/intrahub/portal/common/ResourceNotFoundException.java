package com.intrahub.portal.common;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found with id: " + id);
    }

    public ResourceNotFoundException(String resource, String fieldName, Object fieldValue) {
        super(resource + " not found with " + fieldName + ": " + fieldValue);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
