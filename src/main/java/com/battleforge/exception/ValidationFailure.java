package com.battleforge.exception;

/** A single field rejection, listed under the "errors" property of a ProblemDetail. */
public record ValidationFailure(String field, String message) {
}
