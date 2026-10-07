package br.com.equilibra.attachment.infrastructure;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import static org.assertj.core.api.Assertions.assertThat;

class AttachmentMigrationTest {
    @Test void shouldCreateAttachmentSchemaOnCleanDatabase() throws Exception {
        String url="jdbc:tc:mysql:8.0:///attachment_clean?useSSL=false&allowPublicKeyRetrieval=true";
        try(var connection=DriverManager.getConnection(url,"test","test")){Flyway flyway=Flyway.configure().dataSource(url,"test","test").locations("classpath:db/migration").cleanDisabled(true).load();flyway.migrate();flyway.validate();assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("8");try(var query=connection.prepareStatement("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='transaction_attachments'");var result=query.executeQuery()){assertThat(result.next()).isTrue();assertThat(result.getInt(1)).isEqualTo(1);}}
    }
}
