package br.com.equilibra.shared.web.exception;

import br.com.equilibra.shared.web.filter.RequestIdFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
            .standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .setValidator(validator)
            .addFilters(new RequestIdFilter())
            .build();
    }

    @Test
    void invalidRequestReturnsFieldValidationErrors() throws Exception {
        mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(header().exists(RequestIdFilter.REQUEST_ID_HEADER))
            .andExpect(jsonPath("$.title").value("Validation failed"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors[0].field").value("description"))
            .andExpect(jsonPath("$.errors[0].message").exists())
            .andExpect(jsonPath("$.requestId").exists())
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid-json"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Malformed request"))
            .andExpect(jsonPath("$.detail").value("The request body is invalid or malformed."));
    }

    @Test
    void resourceNotFoundReturnsProblemDetails() throws Exception {
        mockMvc.perform(get("/test/not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Resource not found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Test resource not found."))
            .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void resourceConflictReturnsProblemDetails() throws Exception {
        mockMvc.perform(get("/test/conflict"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Resource conflict"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("Test resource conflict."))
            .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void unexpectedErrorReturnsSafeProblemDetails() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.title").value("Internal server error"))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.detail").value("An unexpected error occurred."))
            .andExpect(jsonPath("$.detail", not(containsString("database password"))))
            .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void validRequestIdIsReusedInProblemDetails() throws Exception {
        String requestId = UUID.randomUUID().toString();

        mockMvc.perform(get("/test/not-found")
                .header(RequestIdFilter.REQUEST_ID_HEADER, requestId))
            .andExpect(status().isNotFound())
            .andExpect(header().string(RequestIdFilter.REQUEST_ID_HEADER, requestId))
            .andExpect(jsonPath("$.requestId").value(requestId));
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @PostMapping("/validation")
        void validate(@Valid @RequestBody TestRequest request) {
            // Controller restrito ao teste para disparar validação Bean Validation.
        }

        @GetMapping("/not-found")
        void notFound() {
            throw new ResourceNotFoundException("Test resource not found.");
        }

        @GetMapping("/conflict")
        void conflict() {
            throw new ResourceConflictException("Test resource conflict.");
        }

        @GetMapping("/unexpected")
        void unexpected() {
            throw new IllegalStateException("Internal database password leaked");
        }
    }

    record TestRequest(@NotBlank String description) {
    }
}
