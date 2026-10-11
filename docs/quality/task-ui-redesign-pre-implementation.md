# UI REDESIGN — PRE-IMPLEMENTATION REPORT

## Referência visual

`equilibra-docs/admin-dashboard.webp` apresenta uma aplicação administrativa com:

- sidebar vertical escura e compacta;
- header/topbar escuro;
- canvas principal cinza-claro;
- cards/painéis brancos com sombras discretas;
- acentos coloridos em indicadores e estados;
- navegação e tabelas densas, mas legíveis.

## Estado atual

O Equilibra já possui Shell, Header, Sidebar, PageHeader, ContentPanel, StatCard, EmptyState, tokens globais e layouts responsivos. As páginas usam Angular Material e devem ser harmonizadas sem alterar APIs ou lógica financeira.

## Estratégia

Atualizar tokens semânticos, superfícies e espaçamentos; adaptar shell/header/sidebar e componentes compartilhados; depois aplicar ajustes de composição às páginas existentes. A sidebar continuará acessível, com drawer no mobile. Cores, raios, sombras, tipografia e breakpoints permanecerão em tokens.

## Escopo visual

Dashboard, Contas, Categorias, Tags, Despesas, Receitas, Transferências, Histórico e Relatórios. Estados loading/empty/error, tabelas/cards mobile, foco e landmarks serão preservados.

## Fora de escopo

Backend, contratos, autenticação, regras financeiras, persistência, novas funcionalidades, exportação e alteração da identidade textual Equilibra.
