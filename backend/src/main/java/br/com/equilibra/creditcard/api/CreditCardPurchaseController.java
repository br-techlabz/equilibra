package br.com.equilibra.creditcard.api;

import br.com.equilibra.creditcard.application.CreditCardPurchaseService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI; import java.util.List;

@RestController @RequestMapping("/credit-card-purchases") @Tag(name="Credit card purchases") @SecurityRequirement(name="bearerAuth")
public class CreditCardPurchaseController {
 private final CreditCardPurchaseService service; public CreditCardPurchaseController(CreditCardPurchaseService s){service=s;}
 @PostMapping public ResponseEntity<CreditCardPurchaseResponse> create(@Valid @RequestBody CreateCreditCardPurchaseRequest r){var response=service.create(r);URI location=ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();return ResponseEntity.created(location).body(response);}
 @GetMapping("/card/{cardId}") public List<CreditCardPurchaseResponse> list(@PathVariable String cardId){return service.list(cardId);}
 @GetMapping("/{id}") public CreditCardPurchaseResponse get(@PathVariable String id){return service.get(id);}
 @PostMapping("/{id}/cancel") public CreditCardPurchaseResponse cancel(@PathVariable String id){return service.cancel(id);}
}
