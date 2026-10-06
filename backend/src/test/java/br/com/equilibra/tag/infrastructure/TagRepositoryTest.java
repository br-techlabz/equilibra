package br.com.equilibra.tag.infrastructure;

import br.com.equilibra.tag.domain.Tag;
import br.com.equilibra.user.domain.User;
import br.com.equilibra.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class TagRepositoryTest {
    @Autowired private TagRepository tags;
    @Autowired private UserRepository users;
    @Autowired private br.com.equilibra.auth.application.PasswordService passwords;
    private String owner;

    @BeforeEach
    void setUp() {
        owner = users.saveAndFlush(new User("tag.repo." + UUID.randomUUID() + "@example.test", passwords.encode("senhaValida123"))).getId();
    }

    @Test
    void shouldListActiveByNameAndIncludeInactive() {
        Tag active = tags.saveAndFlush(new Tag(owner, "Zeta"));
        Tag inactive = new Tag(owner, "Alpha");
        inactive.deactivate();
        tags.saveAndFlush(inactive);
        assertThat(tags.findAllByOwnerIdAndActiveTrueOrderByNameAsc(owner)).extracting(Tag::getId).containsExactly(active.getId());
        assertThat(tags.findAllByOwnerIdOrderByNameAsc(owner)).extracting(Tag::getId).containsExactly(inactive.getId(), active.getId());
    }

    @Test
    void shouldFindOnlyByOwner() {
        String other = users.saveAndFlush(new User("tag.repo.other." + UUID.randomUUID() + "@example.test", passwords.encode("senhaValida123"))).getId();
        Tag tag = tags.saveAndFlush(new Tag(owner, "Privada"));
        assertThat(tags.findByIdAndOwnerId(tag.getId(), owner)).isPresent();
        assertThat(tags.findByIdAndOwnerId(tag.getId(), other)).isEmpty();
    }
}
