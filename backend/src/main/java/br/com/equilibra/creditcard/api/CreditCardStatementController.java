package br.com.equilibra.creditcard.api;
import br.com.equilibra.creditcard.application.CreditCardStatementService; import io.swagger.v3.oas.annotations.security.SecurityRequirement; import io.swagger.v3.oas.annotations.tags.Tag; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @SecurityRequirement(name="bearerAuth") @Tag(name="Credit card statements") public class CreditCardStatementController{
 private final CreditCardStatementService service; public CreditCardStatementController(CreditCardStatementService s){service=s;}
 @GetMapping("/credit-cards/{cardId}/statements") public List<CreditCardStatementResponse> list(@PathVariable String cardId){return service.list(cardId);}
 @GetMapping("/credit-card-statements/{id}") public CreditCardStatementResponse get(@PathVariable String id){return service.get(id);}
 @PostMapping("/credit-cards/{cardId}/statements/close") public ResponseEntity<CreditCardStatementResponse> close(@PathVariable String cardId,@Valid @RequestBody CloseStatementRequest request){return ResponseEntity.ok(service.close(cardId,request));}
}
