package br.com.equilibra.attachment.infrastructure;

import br.com.equilibra.attachment.application.AttachmentProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileSystemAttachmentStorageTest {
    @TempDir Path temp;
    @Test void shouldStoreReadAndDeleteWithoutPublicPath() throws Exception {AttachmentProperties p=new AttachmentProperties();p.setLocalRoot(temp.toString());LocalFileSystemAttachmentStorage storage=new LocalFileSystemAttachmentStorage(p);assertThatCode(() -> storage.store("owner/tx/file",new ByteArrayInputStream("data".getBytes()),4)).doesNotThrowAnyException();assertThat(storage.exists("owner/tx/file")).isTrue();assertThatThrownBy(()->storage.open("../escape")).isInstanceOf(IllegalArgumentException.class);}
}
