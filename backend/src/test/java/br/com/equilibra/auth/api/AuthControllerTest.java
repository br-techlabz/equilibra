package br.com.equilibra.auth.api;

import br.com.equilibra.auth.application.RegisterUserCommand;
import br.com.equilibra.auth.application.RegisterUserResult;
import br.com.equilibra.auth.application.RegisterUserService;
import br.com.equilibra.shared.web.exception.GlobalExceptionHandler;
import br.com.equilibra.shared.web.exception.ResourceConflictException;
import br.com.equilibra.shared.web.filter.RequestIdFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private RegisterUserService registerUserService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        registerUserService = mock(RegisterUserService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
            .standaloneSetup(new AuthController(registerUserService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .setValidator(validator)
            .addFilters(new RequestIdFilter())
            .build();
    }

    @Test
    void shouldReturnCreatedForValidRequestWithoutSensitiveFields() throws Exception {
        when(registerUserService.register(any(RegisterUserCommand.class))).thenReturn(
            new RegisterUserResult("123e4567-e89b-12d3-a456-426614174000", "bill@example.com", Instant.parse("2026-10-04T12:00:00Z"))
        );

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": " Bill@Example.com ",
                      "password": "senhaValida123"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(header().exists(RequestIdFilter.REQUEST_ID_HEADER))
            .andExpect(jsonPath("$.id").value("123e4567-e89b-12d3-a456-426614174000"))
            .andExpect(jsonPath("$.email").value("bill@example.com"))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(content().string(not(containsString("password"))))
            .andExpect(content().string(not(containsString("passwordHash"))))
            .andExpect(content().string(not(containsString("senhaValida123"))));

        verify(registerUserService).register(new RegisterUserCommand(" Bill@Example.com ", "senhaValida123"));
    }

    @Test
    void shouldReturnBadRequestForInvalidEmail() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "not-an-email",
                      "password": "senhaValida123"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Validation failed"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors[0].field").value("email"))
            .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void shouldReturnBadRequestForInvalidPassword() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "valid@example.com",
                      "password": "curta"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Validation failed"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors[0].field").value("password"))
            .andExpect(jsonPath("$.errors[0].message").value("Senha deve possuir pelo menos 8 caracteres"))
            .andExpect(jsonPath("$.requestId").exists())
            .andExpect(content().string(not(containsString("curta"))));
    }

    @Test
    void shouldReturnConflictForExistingEmail() throws Exception {
        when(registerUserService.register(any(RegisterUserCommand.class))).thenThrow(
            new ResourceConflictException("An account with this email already exists.")
        );

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "duplicado@example.com",
                      "password": "senhaValida123"
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Resource conflict"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("An account with this email already exists."))
            .andExpect(jsonPath("$.requestId").exists())
            .andExpect(content().string(not(containsString("senhaValida123"))))
            .andExpect(content().string(not(containsString("passwordHash"))));
    }
}
