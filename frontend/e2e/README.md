# E2E desktop/mobile

A infraestrutura E2E usa Playwright e executa o frontend Angular em `http://127.0.0.1:4201`.

## Pré-requisitos

1. Inicie o backend com o profile `dev` e banco MySQL disponível:

   ```bash
   cd ../backend
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

2. Instale os navegadores Playwright uma vez:

   ```bash
   npx playwright install chromium
   ```

O frontend é iniciado automaticamente pelo Playwright quando a porta 4201 estiver livre. Se já houver um `ng serve` nessa porta, ele será reutilizado localmente.

## Comandos

```bash
npm run e2e          # desktop, mobile e tablet
npm run e2e:desktop  # Desktop Chrome 1280x720
npm run e2e:mobile   # Mobile 390x844
npm run e2e:report   # Abre o relatório HTML
```

Os testes registram usuários com e-mails únicos pela interface e fazem login pela UI. O JWT não é persistido em `localStorage` ou `sessionStorage`.

Os artefatos de falha ficam em `test-results/` e o relatório em `playwright-report/`.
