package com.dockerplatform.backend.exceptions;

import org.springframework.security.core.AuthenticationException;

public class PasswordChangeRequiredException extends AuthenticationException {
    public PasswordChangeRequiredException() {
        super("Must change initial password.");    }
}
