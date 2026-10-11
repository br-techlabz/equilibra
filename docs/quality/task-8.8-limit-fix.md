# Correção pós-gate — Validação de limite na compra

## Problema

O card e o combobox exibiam o limite disponível descontando pagamentos, mas o serviço de criação da compra validava apenas a soma bruta das compras ativas. Depois de pagamentos, as duas fontes divergiam e uma compra válida era rejeitada.

## Correção

A validação de limite na criação agora usa a mesma regra do card:

```text
compras ativas
− pagamentos ACTIVE das faturas
= comprometimento atual
```

A nova compra é aceita quando:

```text
comprometimento atual + nova compra <= limite total
```

## Validação

```bash
cd backend
./mvnw test -q
./mvnw package -q -DskipTests
```

Resultados: aprovados.
