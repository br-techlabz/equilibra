package br.com.equilibra.attachment.api;

import br.com.equilibra.attachment.application.AttachmentService;
import br.com.equilibra.attachment.domain.TransactionAttachment;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@Tag(name="Attachments",description="Anexos privados de transações")
@SecurityRequirement(name="bearerAuth")
public class AttachmentController {
 private final AttachmentService service; public AttachmentController(AttachmentService service){this.service=service;}
 @PostMapping(value="/transactions/{transactionId}/attachments",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public ResponseEntity<AttachmentResponse> upload(@PathVariable String transactionId,@RequestPart("file") MultipartFile file){return ResponseEntity.status(HttpStatus.CREATED).body(service.upload(transactionId,file));}
 @GetMapping("/transactions/{transactionId}/attachments") public List<AttachmentResponse> list(@PathVariable String transactionId){return service.list(transactionId);}
 @GetMapping("/attachments/{id}/content") public ResponseEntity<InputStreamResource> content(@PathVariable String id){TransactionAttachment a=service.find(id);return ResponseEntity.ok().contentType(MediaType.parseMediaType(a.getContentType())).contentLength(a.getSizeBytes()).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+a.getOriginalFileName()+"\"").header("X-Content-Type-Options","nosniff").cacheControl(CacheControl.noStore().cachePrivate()).body(new InputStreamResource(service.open(id)));}
 @DeleteMapping("/attachments/{id}") public ResponseEntity<Void> delete(@PathVariable String id){service.delete(id);return ResponseEntity.noContent().build();}
}
