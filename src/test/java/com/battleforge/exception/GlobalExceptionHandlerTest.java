package com.battleforge.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Standalone MockMvc over a throwaway controller: this asserts the shape of the error
 * contract itself, without dragging in a Spring context or a database.
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void invalidBodyYieldsProblemDetailWithFieldErrors() throws Exception {
        mockMvc.perform(post("/probe/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("https://battleforge.dev/problems/validation-failed"))
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void missingResourceYields404() throws Exception {
        mockMvc.perform(get("/probe/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.detail").value("Team not found: 42"));
    }

    @Test
    void rejectedBattleActionYields409() throws Exception {
        mockMvc.perform(get("/probe/illegal-action"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Invalid battle action"))
                .andExpect(jsonPath("$.type").value("https://battleforge.dev/problems/invalid-battle-action"));
    }

    @Test
    void unexpectedFailureYields500WithTraceIdAndNoInternalDetail() throws Exception {
        mockMvc.perform(get("/probe/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andExpect(jsonPath("$.detail").value("Unexpected error. Quote the traceId when reporting it."));
    }

    /**
     * Called directly rather than through MockMvc: only a servlet container with the static
     * resource handler in place raises NoResourceFoundException, and standalone setup raises
     * NoHandlerFoundException instead.
     */
    @Test
    void unknownEndpointYields404WithoutStaticResourceWording() {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "api/does-not-exist");

        ResponseEntity<Object> response = new GlobalExceptionHandler()
                .handleNoResourceFoundException(ex, new HttpHeaders(), HttpStatus.NOT_FOUND, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isInstanceOf(ProblemDetail.class);

        ProblemDetail problem = (ProblemDetail) response.getBody();
        assertThat(problem.getTitle()).isEqualTo("Endpoint not found");
        assertThat(problem.getDetail()).isEqualTo("No endpoint GET api/does-not-exist.");
        assertThat(problem.getType())
                .hasToString("https://battleforge.dev/problems/endpoint-not-found");
    }

    @RestController
    @RequestMapping("/probe")
    static class ProbeController {

        record EchoRequest(@NotBlank String name) {
        }

        @PostMapping("/echo")
        String echo(@Valid @RequestBody EchoRequest request) {
            return request.name();
        }

        @GetMapping("/missing")
        String missing() {
            throw ResourceNotFoundException.of("Team", 42);
        }

        @GetMapping("/illegal-action")
        String illegalAction() {
            throw new InvalidBattleActionException("Battle is awaiting a switch");
        }

        @GetMapping("/boom")
        String boom() {
            throw new IllegalStateException("database credentials rejected");
        }
    }
}
