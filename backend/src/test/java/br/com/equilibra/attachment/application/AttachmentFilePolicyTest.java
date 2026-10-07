package br.com.equilibra.attachment.application;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttachmentFilePolicyTest {
    private final AttachmentProperties properties = new AttachmentProperties();
    private final AttachmentFilePolicy policy = new AttachmentFilePolicy(properties);

    @Test void shouldAcceptPdfSignature() throws Exception {byte[] pdf="%PDF-1.7 test".getBytes();assertThat(policy.validateAndDetect(new ByteArrayInputStream(pdf),"application/pdf",pdf.length)).isEqualTo("application/pdf");}
    @Test void shouldRejectSpoofedPdf(){byte[] bytes="not a pdf".getBytes();assertThatThrownBy(()->policy.validateAndDetect(new ByteArrayInputStream(bytes),"application/pdf",bytes.length)).isInstanceOf(IllegalArgumentException.class);}
    @Test void shouldRejectEmptyAndUnsupported(){assertThatThrownBy(()->policy.validateAndDetect(new ByteArrayInputStream(new byte[0]),"application/pdf",0)).isInstanceOf(IllegalArgumentException.class);assertThatThrownBy(()->policy.validateAndDetect(new ByteArrayInputStream(new byte[]{1}),"text/html",1)).isInstanceOf(IllegalArgumentException.class);}
    @Test void shouldRejectUnsafeNames(){assertThatThrownBy(()->br.com.equilibra.attachment.domain.TransactionAttachment.validateFileName("../../secret.pdf")).isInstanceOf(IllegalArgumentException.class);assertThatThrownBy(()->br.com.equilibra.attachment.domain.TransactionAttachment.validateFileName("bad\nname.pdf")).isInstanceOf(IllegalArgumentException.class);}
}
